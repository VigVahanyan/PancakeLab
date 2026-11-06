package org.pancakelab.model;

import org.pancakelab.api.model.Ingredient;
import org.pancakelab.common.OrderOperation;
import org.pancakelab.common.OrderStatus;
import org.pancakelab.common.OrderWorkflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An order and the rules about itself: every change goes through a behaviour method that checks
 * {@link OrderWorkflow} (is the operation allowed in the current status?) and the order's own
 * invariants. The status and the pancakes cannot be changed from outside. Illegal moves throw
 * {@link IllegalStateException}, bad arguments {@link IllegalArgumentException}.
 * <p>
 * Things that need collaborators (building registry, ingredient catalog, storage, logging) are not
 * its concern; the services handle those.
 * <p>
 * Thread safety: each order is its own lock. Every method that reads or changes its state is
 * {@code synchronized}, so a check-then-change (e.g. "is it still open?" then "complete it") is
 * atomic per order, while different orders never block each other. Getters hand out snapshots, so a
 * caller never iterates a list another thread is changing.
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

    public synchronized OrderStatus getStatus() {
        return status;
    }

    /**
     * Read-only snapshot of the pancakes in insertion order.
     */
    public synchronized List<Pancake> getPancakes() {
        return List.copyOf(pancakes);
    }

    public synchronized List<String> descriptions() {
        return pancakes.stream().map(Pancake::description).toList();
    }

    /**
     * Starts a new, empty pancake.
     *
     * @return the id of the new pancake
     */
    public synchronized UUID addPancake() {
        OrderStatus next = OrderWorkflow.next(status, OrderOperation.ADD_PANCAKE);
        Pancake pancake = new Pancake();
        pancakes.add(pancake);
        status = next;
        return pancake.getId();
    }

    public synchronized void addIngredient(UUID pancakeId, Ingredient ingredient) {
        OrderStatus next = OrderWorkflow.next(status, OrderOperation.ADD_INGREDIENT);
        pancakes.stream().filter(p -> p.getId().equals(pancakeId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pancake %s is not in order %s".formatted(pancakeId, id)))
                .addIngredient(ingredient);
        status = next;
    }

    /**
     * Removes the first {@code count} pancakes whose description matches.
     */
    public synchronized void removePancakes(String description, int count) {
        OrderStatus next = OrderWorkflow.next(status, OrderOperation.REMOVE_PANCAKES);
        List<Pancake> matching = pancakes.stream().filter(p -> p.description().equals(description)).toList();
        if (matching.size() < count) {
            throw new IllegalArgumentException("Cannot remove %d pancake(s) '%s': order contains only %d"
                    .formatted(count, description, matching.size()));
        }
        pancakes.removeAll(matching.subList(0, count));
        status = next;
    }

    /**
     * Completes the order; it must have at least one pancake and every pancake needs an ingredient.
     */
    public synchronized void complete() {
        OrderStatus next = OrderWorkflow.next(status, OrderOperation.COMPLETE);
        if (pancakes.isEmpty()) {
            throw new IllegalStateException("Cannot complete an empty order " + id);
        }
        if (pancakes.stream().anyMatch(p -> p.getIngredients().isEmpty())) {
            throw new IllegalStateException("Cannot complete order %s: a pancake has no ingredients".formatted(id));
        }
        status = next;
    }

    public synchronized void prepare() {
        status = OrderWorkflow.next(status, OrderOperation.PREPARE);
    }

    public synchronized void cancel() {
        status = OrderWorkflow.next(status, OrderOperation.CANCEL);
    }

    public synchronized void deliver() {
        status = OrderWorkflow.next(status, OrderOperation.DELIVER);
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
