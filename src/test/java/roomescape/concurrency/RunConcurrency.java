package roomescape.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RunConcurrency {

    public static Result run(Runnable... tasks) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(tasks.length);
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.length);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for(Runnable task:tasks) {
            executorService.execute(() -> {
                try {
                    task.run();
                    successCount.getAndIncrement();
                } catch(Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        return new Result(successCount.get(), failCount.get());
    }

    public static Result run(int thread, Runnable task) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(thread);
        ExecutorService executorService = Executors.newFixedThreadPool(thread);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for(int i = 0; i < thread; i++) {
            executorService.execute(() -> {
                try {
                    task.run();
                    successCount.getAndIncrement();
                } catch(Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        return new Result(successCount.get(), failCount.get());
    }
}
