package org.pancakelab;

import org.pancakelab.api.ChefApi;
import org.pancakelab.api.DeliveryApi;
import org.pancakelab.api.DiscipleApi;
import org.pancakelab.api.model.DeliveredOrder;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.pancakes.IngredientCatalog;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.OrderServiceImpl;
import org.pancakelab.service.pancake.PancakeServiceImpl;
import org.pancakelab.service.role.ChefApiImpl;
import org.pancakelab.service.role.DeliveryApiImpl;
import org.pancakelab.service.role.DiscipleApiImpl;
import org.pancakelab.service.shared.OrderLog;

import java.util.UUID;

/**
 * Demo of the client API: a disciple orders, the chef prepares, delivery brings the pancakes.
 */
public class Main {
    public static void main(String[] args) {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog();
        OrderService orders = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);

        DiscipleApi disciple = new DiscipleApiImpl(
                orders, new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log));
        ChefApi chef = new ChefApiImpl(orders);
        DeliveryApi delivery = new DeliveryApiImpl(orders);

        // Disciple
        UUID orderId = disciple.createOrder(10, 20);
        UUID pancake = disciple.addPancake(orderId);
        disciple.listIngredients().forEach(i -> System.out.println("Menu: " + i.id() + " " + i.description()));
        disciple.addIngredientToPancake(orderId, pancake, 2); // milk chocolate
        disciple.addIngredientToPancake(orderId, pancake, 4); // hazelnuts
        disciple.completeOrder(orderId);
        System.out.println("Order: " + disciple.viewOrder(orderId));

        // Chef
        for (UUID id : chef.listCompletedOrders()) {
            chef.prepareOrder(id);
        }

        // Delivery
        for (UUID id : delivery.listPreparedOrders()) {
            DeliveredOrder delivered = delivery.deliverOrder(id);
            System.out.printf("Deliver to building %d, room %d: %s%n",
                    delivered.building(), delivered.room(), delivered.pancakes());
        }
    }
}
