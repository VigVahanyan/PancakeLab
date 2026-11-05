package org.pancakelab.service.role;

import org.pancakelab.api.ChefApi;
import org.pancakelab.api.DeliveryApi;
import org.pancakelab.api.DiscipleApi;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.pancakes.IngredientCatalog;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.OrderServiceImpl;
import org.pancakelab.service.pancake.PancakeServiceImpl;
import org.pancakelab.service.shared.OrderLog;

import java.util.UUID;

import static org.pancakelab.service.TestIngredients.DARK_CHOCOLATE;

/**
 * Test fixture: the three role APIs wired over one shared repository, with a silent log.
 */
final class TestShop {
    final DiscipleApi disciple;
    final ChefApi chef;
    final DeliveryApi delivery;

    TestShop() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog(message -> {
        });
        OrderService orders = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        this.disciple = new DiscipleApiImpl(
                orders, new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log));
        this.chef = new ChefApiImpl(orders);
        this.delivery = new DeliveryApiImpl(orders);
    }

    /**
     * An order for building 10, room 20 with one dark chocolate pancake, not yet completed.
     */
    UUID orderWithPancake() {
        UUID orderId = disciple.createOrder(10, 20);
        UUID pancakeId = disciple.addPancake(orderId);
        disciple.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        return orderId;
    }

    UUID completedOrder() {
        UUID orderId = orderWithPancake();
        disciple.completeOrder(orderId);
        return orderId;
    }

    UUID preparedOrder() {
        UUID orderId = completedOrder();
        chef.prepareOrder(orderId);
        return orderId;
    }
}
