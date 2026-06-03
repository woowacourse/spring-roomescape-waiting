package roomescape.infra.queue;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public abstract class AsyncConsumer<T, R> implements ApplicationRunner {

    private final AsyncQueue<T, R> queue;
    private volatile Thread consumerThread;

    protected AsyncConsumer(AsyncQueue<T, R> queue) {
        this.queue = queue;
    }

    @Override
    public void run(ApplicationArguments args) {
        consumerThread = Thread.ofVirtual().name(threadName()).start(this::consumeLoop);
    }

    @PreDestroy
    public void stop() {
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    protected abstract String threadName();

    protected abstract JobResult<R> process(T request);

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                AsyncMessage<T> msg = queue.take();
                JobResult<R> result = process(msg.request());
                queue.storeResult(msg.jobId(), result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
