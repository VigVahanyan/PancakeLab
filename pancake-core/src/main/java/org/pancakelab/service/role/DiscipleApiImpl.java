package org.pancakelab.service.role;

import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.api.DiscipleApi;
import org.pancakelab.api.model.Ingredient;

import java.util.List;
import java.util.UUID;

/** Disciple use-cases, delegating to the order and pancake services. */
public class DiscipleApiImpl implements DiscipleApi {
    private final OrderService   orders;
    private final PancakeService pancakes;

    public DiscipleApiImpl(OrderService orders, PancakeService pancakes) {
        this.orders   = orders;
        this.pancakes = pancakes;
    }

    @Override
    public UUID createOrder(int building, int room) {
        return orders.createOrder(building, room);
    }

    @Override
    public UUID addPancake(UUID orderId) {
        return pancakes.addPancake(orderId);
    }

    @Override
    public List<Ingredient> listIngredients() {
        return pancakes.listIngredients();
    }

    @Override
    public void addIngredientToPancake(UUID orderId, UUID pancakeId, int ingredientId) {
        pancakes.addIngredientToPancake(orderId, pancakeId, ingredientId);
    }

    @Override
    public void removePancakes(String description, UUID orderId, int count) {
        pancakes.removePancakes(description, orderId, count);
    }

    @Override
    public List<String> viewOrder(UUID orderId) {
        return orders.viewOrder(orderId);
    }

    @Override
    public void completeOrder(UUID orderId) {
        orders.completeOrder(orderId);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        orders.cancelOrder(orderId);
    }
}
