package roomescape.domain.waiting;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.domain.waiting.dto.WaitingResult;
import roomescape.exception.RoomescapeException;

@Component
public class WaitingConsumer implements ApplicationRunner {

    private final WaitingQueue waitingQueue;
    private final WaitingService waitingService;
    private volatile Thread consumerThread;

    public WaitingConsumer(WaitingQueue waitingQueue, WaitingService waitingService) {
        this.waitingQueue = waitingQueue;
        this.waitingService = waitingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        consumerThread = Thread.ofVirtual().name("waiting-consumer").start(this::consumeLoop);
    }

    @PreDestroy
    public void stop() {
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WaitingMessage msg = waitingQueue.take();
                process(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void process(WaitingMessage msg) {
        try {
            WaitingResponse result = waitingService.createWaiting(msg.request());
            waitingQueue.storeResult(msg.jobId(), WaitingResult.success(result));
        } catch (RoomescapeException e) {
            waitingQueue.storeResult(msg.jobId(), WaitingResult.failed(e.getMessage()));
        }
    }
}