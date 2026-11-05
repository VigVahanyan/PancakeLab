package org.pancakelab.service.order;

import org.pancakelab.api.model.DeliveredOrder;
import org.pancakelab.common.OrderOperation;
import org.pancakelab.common.OrderStatus;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.Order;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.service.shared.OrderLog;
import org.pancakelab.service.shared.OrderLookup;

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
        log.log("CREATE", order);
        return order.getId();
    }

    @Override
    public List<String> viewOrder(UUID orderId) {
        return lookup.find(orderId).descriptions();
    }

    @Override
    public void completeOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(order::complete);
        log.log(OrderOperation.COMPLETE, order);
    }

    @Override
    public void prepareOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(order::prepare);
        log.log(OrderOperation.PREPARE, order);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(order::cancel);
        orders.remove(orderId);
        log.log(OrderOperation.CANCEL, order);
    }

    @Override
    public DeliveredOrder deliverOrder(UUID orderId) {
        Order order = lookup.find(orderId);
        guarded(order::deliver);
        DeliveredOrder delivered = new DeliveredOrder(order.getId(), order.getBuilding(),
                order.getRoom(), order.descriptions());
        orders.remove(orderId);
        log.log(OrderOperation.DELIVER, order);
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
