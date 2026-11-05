package org.pancakelab.model;

import org.pancakelab.common.OrderStatus;
import org.pancakelab.model.pancakes.Pancake;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Plain data holder: identity, delivery address, status and pancakes. It has no rules of its own;
 * the services enforce them.
 */
public class Order {
    private final UUID id = UUID.randomUUID();
    private final int building;
    private final int room;
    private final List<Pancake> pancakes = new ArrayList<>();
    private OrderStatus status = OrderStatus.CREATED;

    public Order(int building, int room) {
        this.building = building;
        this.room = room;
    }

    public UUID getId() {
        return id;
    }

    public int getBuilding() {
        return building;
    }

    public int getRoom() {
        return room;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * The live, mutable list of pancakes in insertion order.
     */
    public List<Pancake> getPancakes() {
        return pancakes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Order) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
