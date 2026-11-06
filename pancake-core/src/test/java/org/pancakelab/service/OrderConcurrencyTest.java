package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.common.InvalidOrderStateException;
import org.pancakelab.common.OrderNotFoundException;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.pancakes.IngredientCatalog;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.OrderServiceImpl;
import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.service.pancake.PancakeServiceImpl;
import org.pancakelab.service.shared.OrderLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.pancakelab.service.TestIngredients.DARK_CHOCOLATE;

/**
 * Many threads working on the same order must leave it consistent: no lost updates, no
 * double transitions, no exceptions from concurrent reads.
 */
public class OrderConcurrencyTest {
    private static final int ROUNDS = 200;

    private OrderService orderService;
    private PancakeService pancakeService;

    @BeforeEach
    public void setUp() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog(message -> {
        });
        orderService = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        pancakeService = new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log);
    }

    @Test
    public void GivenOrder_WhenThreadsAddPancakesConcurrently_ThenNoPancakeIsLost_Test() throws Exception {
        // setup
        UUID orderId = orderService.createOrder(10, 20);
        int threads = 8;
        int perThread = 200;

        // exercise
        List<Throwable> errors = runInParallel(threads, () -> {
            for (int i = 0; i < perThread; i++) {
                pancakeService.addPancake(orderId);
            }
        });

        // verify
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(threads * perThread, orderService.viewOrder(orderId).size());
    }

    @Test
    public void GivenPancake_WhenThreadsAddIngredientsConcurrently_ThenNoIngredientIsLost_Test() throws Exception {
        // setup
        UUID orderId = orderService.createOrder(10, 20);
        UUID pancakeId = pancakeService.addPancake(orderId);
        int threads = 8;
        int perThread = 100;

        // exercise
        List<Throwable> errors = runInParallel(threads, () -> {
            for (int i = 0; i < perThread; i++) {
                pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
            }
        });

        // verify
        assertTrue(errors.isEmpty(), errors.toString());
        String description = orderService.viewOrder(orderId).get(0);
        assertEquals(threads * perThread, description.split(", ").length);
    }

    @Test
    public void GivenPreparedOrder_WhenThreadsDeliverIt_ThenExactlyOneSucceeds_Test() throws Exception {
        for (int round = 0; round < ROUNDS; round++) {
            // setup
            UUID orderId = preparedOrder();
            AtomicInteger deliveries = new AtomicInteger();
            int threads = 8;

            // exercise
            List<Throwable> errors = runInParallel(threads, () -> {
                orderService.deliverOrder(orderId);
                deliveries.incrementAndGet();
            });

            // verify
            assertEquals(1, deliveries.get(), "round " + round);
            assertEquals(threads - 1, errors.size());
            errors.forEach(e -> assertTrue(
                    e instanceof InvalidOrderStateException || e instanceof OrderNotFoundException, e.toString()));
        }
    }

    @Test
    public void GivenOrder_WhenCompletedAndCancelledAtTheSameTime_ThenExactlyOneWins_Test() throws Exception {
        for (int round = 0; round < ROUNDS; round++) {
            // setup
            UUID orderId = orderService.createOrder(10, 20);
            UUID pancakeId = pancakeService.addPancake(orderId);
            pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
            AtomicInteger wins = new AtomicInteger();

            // exercise
            List<Throwable> errors = runInParallel(
                    () -> {
                        orderService.completeOrder(orderId);
                        wins.incrementAndGet();
                    },
                    () -> {
                        orderService.cancelOrder(orderId);
                        wins.incrementAndGet();
                    });

            // verify
            assertEquals(1, wins.get(), "round " + round);
            assertEquals(1, errors.size());
        }
    }

    @Test
    public void GivenOrder_WhenReadingWhileThreadsModifyIt_ThenNoReaderFails_Test() throws Exception {
        // setup
        UUID orderId = orderService.createOrder(10, 20);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tasks.add(() -> {
                for (int n = 0; n < 300; n++) {
                    UUID pancakeId = pancakeService.addPancake(orderId);
                    pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
                }
            });
            tasks.add(() -> {
                for (int n = 0; n < 300; n++) {
                    orderService.viewOrder(orderId);
                }
            });
        }

        // exercise
        List<Throwable> errors = runInParallel(tasks.toArray(new Runnable[0]));

        // verify
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(4 * 300, orderService.viewOrder(orderId).size());
    }

    private UUID preparedOrder() {
        UUID orderId = orderService.createOrder(10, 20);
        UUID pancakeId = pancakeService.addPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        orderService.completeOrder(orderId);
        orderService.prepareOrder(orderId);
        return orderId;
    }

    /**
     * Runs the same task on the given number of threads, released together.
     */
    private static List<Throwable> runInParallel(int threads, Runnable task) throws InterruptedException {
        Runnable[] tasks = new Runnable[threads];
        java.util.Arrays.fill(tasks, task);
        return runInParallel(tasks);
    }

    /**
     * Runs every task on its own thread, released together; returns what they threw.
     */
    private static List<Throwable> runInParallel(Runnable... tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch ready = new CountDownLatch(tasks.length);
        CountDownLatch start = new CountDownLatch(1);
        Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
        for (Runnable task : tasks) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.run();
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "tasks did not finish");
        return List.copyOf(errors);
    }
}
