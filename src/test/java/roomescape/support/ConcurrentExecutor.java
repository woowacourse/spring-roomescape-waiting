package roomescape.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ConcurrentExecutor {

    public static List<ConcurrentResult> executeConcurrently(
            int threadCount,
            Callable<ConcurrentResult> task
    ) throws InterruptedException {
        List<Callable<ConcurrentResult>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(task);
        }

        return executeConcurrently(tasks);
    }

    public static List<ConcurrentResult> executeConcurrently(
            List<Callable<ConcurrentResult>> tasks
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());

        try {
            CountDownLatch readyLatch = new CountDownLatch(tasks.size());
            CountDownLatch startLatch = new CountDownLatch(1);

            List<Future<ConcurrentResult>> futures = new ArrayList<>();

            for (Callable<ConcurrentResult> task : tasks) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();

                    return task.call();
                }));
            }

            readyLatch.await();
            startLatch.countDown();

            List<ConcurrentResult> results = new ArrayList<>();

            for (Future<ConcurrentResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e.getCause());
                }
            }

            return results;
        } finally {
            executorService.shutdown();
        }
    }
}
