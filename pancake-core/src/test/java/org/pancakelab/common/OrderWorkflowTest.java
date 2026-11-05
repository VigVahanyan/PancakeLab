package org.pancakelab.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.common.OrderOperation.*;
import static org.pancakelab.common.OrderStatus.*;

public class OrderWorkflowTest {
    /**
     * The complete, expected lifecycle: status -> (operation -> next status).
     */
    private static final Map<OrderStatus, Map<OrderOperation, OrderStatus>> EXPECTED = Map.of(
            CREATED, Map.of(ADD_PANCAKE, CREATED, ADD_INGREDIENT, CREATED, REMOVE_PANCAKES, CREATED,
                    COMPLETE, COMPLETED, CANCEL, CANCELLED),
            COMPLETED, Map.of(PREPARE, PREPARED),
            PREPARED, Map.of(DELIVER, DELIVERED),
            DELIVERED, Map.of(),
            CANCELLED, Map.of());

    static Stream<Arguments> allowedTransitions() {
        return EXPECTED.entrySet().stream().flatMap(status ->
                status.getValue().entrySet().stream().map(op ->
                        Arguments.of(status.getKey(), op.getKey(), op.getValue())));
    }

    static Stream<Arguments> forbiddenTransitions() {
        return EXPECTED.entrySet().stream().flatMap(status ->
                EnumSet.complementOf(operationsOf(status.getValue().keySet())).stream().map(op ->
                        Arguments.of(status.getKey(), op)));
    }

    private static EnumSet<OrderOperation> operationsOf(Set<OrderOperation> operations) {
        return operations.isEmpty() ? EnumSet.noneOf(OrderOperation.class) : EnumSet.copyOf(operations);
    }

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @MethodSource("allowedTransitions")
    public void GivenStatus_WhenOperationIsAllowed_ThenMovesToNextStatus_Test(
            OrderStatus status, OrderOperation operation, OrderStatus expectedNext) {
        // setup

        // exercise
        OrderStatus next = OrderWorkflow.next(status, operation);

        // verify
        assertEquals(expectedNext, next);
        assertTrue(OrderWorkflow.allows(status, operation));

        // tear down
    }

    @ParameterizedTest(name = "{0} --{1}--> forbidden")
    @MethodSource("forbiddenTransitions")
    public void GivenStatus_WhenOperationIsNotAllowed_ThenIllegalStateWithAllowedOperations_Test(
            OrderStatus status, OrderOperation operation) {
        // setup

        // exercise
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> OrderWorkflow.next(status, operation));

        // verify
        assertFalse(OrderWorkflow.allows(status, operation));
        assertTrue(e.getMessage().contains(status.name()), e.getMessage());
        assertTrue(e.getMessage().contains(operation.name()), e.getMessage());

        // tear down
    }

    @Test
    public void GivenFinalStatuses_WhenAskingForAllowedOperations_ThenNoneAvailable_Test() {
        assertEquals(Set.of(), OrderWorkflow.allowedOperations(DELIVERED));
        assertEquals(Set.of(), OrderWorkflow.allowedOperations(CANCELLED));
        assertEquals(Set.of(PREPARE), OrderWorkflow.allowedOperations(COMPLETED));
    }
}
