package org.pancakelab.service.order;

import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.service.shared.OrderLog;
import org.pancakelab.service.shared.OrderLookup;
import org.pancakelab.service.shared.OrderRules;
import org.pancakelab.api.model.DeliveredOrder;
import org.pancakelab.common.OrderOperation;
import org.pancakelab.common.OrderWorkflow;
import org.pancakelab.common.OrderStatus;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.Order;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.pancakelab.service.shared.OrderLookup.guarded;

/**
 * Order lifecycle: create, view, complete, prepare, cancel, deliver and list. Pancake contents
 * are handled by {@link PancakeService}. Only ids, strings and {@link DeliveredOrder} cross the
 * API.
 */
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orders;
    private final OrderLookup lookup;
    private final BuildingRegistry buildings;
    private final OrderLog log;

    public OrderServiceImpl(OrderRepository orders, BuildingRegistry buildings, OrderLog log) {
        this.orders = orders;
        this.lookup = new OrderLookup(orders);
        this.buildings = buildings;
        this.log = log;
    }

    @Override
    public UUID createOrder(int building, int room) {
        buildings.validate(building, room);
        Order order = new Order(building, room);
        orders.save(order);
        return order.getId();
    }

    @Override
    public List<String> viewOrder(UUID orderId) {
        return OrderRules.descriptions(lookup.find(orderId));
    }

    @Override
    public void completeOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(() -> {
            OrderStatus next = OrderWorkflow.next(order.getStatus(), OrderOperation.COMPLETE);
            if (order.getPancakes().isEmpty()) {
                throw new IllegalStateException("Cannot complete an empty order " + order.getId());
            }
            if (order.getPancakes().stream().anyMatch(p -> p.getIngredients().isEmpty())) {
                throw new IllegalStateException(
                        "Cannot complete order %s: a pancake has no ingredients".formatted(order.getId()));
            }
            order.setStatus(next);
        });
        log.logCompleteOrder(order);
    }

    @Override
    public void prepareOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(() -> OrderRules.transition(order, OrderOperation.PREPARE));
        log.logPrepareOrder(order);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(() -> OrderRules.transition(order, OrderOperation.CANCEL));
        orders.remove(orderId);
        log.logCancelOrder(order);
    }

    @Override
    public DeliveredOrder deliverOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(() -> OrderRules.transition(order, OrderOperation.DELIVER));
        DeliveredOrder delivered = new DeliveredOrder(order.getId(), order.getBuilding(),
                order.getRoom(), OrderRules.descriptions(order));
        orders.remove(orderId);
        log.logDeliverOrder(order);
        return delivered;
    }

    @Override
    public Set<UUID> listCompletedOrders() {
        return orders.findIdsByStatus(OrderStatus.COMPLETED);
    }

    @Override
    public Set<UUID> listPreparedOrders() {
        return orders.findIdsByStatus(OrderStatus.PREPARED);
    }
}
