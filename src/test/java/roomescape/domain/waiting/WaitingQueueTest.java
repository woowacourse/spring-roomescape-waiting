package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.infra.queue.AsyncMessage;
import roomescape.infra.queue.JobResult;

class WaitingQueueTest {

    private WaitingQueue waitingQueue;
    private WaitingRequest request;

    @BeforeEach
    void setUp() {
        waitingQueue = new WaitingQueue();
        request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
    }

    @Nested
    @DisplayName("enqueue 테스트")
    class Enqueue {

        @Test
        void enqueue_하면_PENDING_상태로_등록된다() {
            String jobId = waitingQueue.enqueue(request);

            JobResult result = waitingQueue.getResult(jobId);

            assertAll(
                    () -> assertThat(jobId).isNotNull(),
                    () -> assertThat(result.status()).isEqualTo("PENDING"),
                    () -> assertThat(result.data()).isNull(),
                    () -> assertThat(result.errorMessage()).isNull()
            );
        }

        @Test
        void enqueue_하면_큐에서_take로_꺼낼_수_있다() throws InterruptedException {
            waitingQueue.enqueue(request);

            AsyncMessage message = waitingQueue.take();

            assertAll(
                    () -> assertThat(message.request()).isEqualTo(request),
                    () -> assertThat(message.jobId()).isNotNull()
            );
        }
    }

    @Nested
    @DisplayName("storeResult 테스트")
    class StoreResult {

        @Test
        void storeResult_하면_getResult로_저장된_결과를_반환한다() {
            String jobId = waitingQueue.enqueue(request);
            WaitingResponse response = new WaitingResponse(1L, "유저1", LocalDate.of(2099, 12, 31), 1L, 1L);

            waitingQueue.storeResult(jobId, JobResult.success(response));

            JobResult result = waitingQueue.getResult(jobId);
            assertAll(
                    () -> assertThat(result.status()).isEqualTo("SUCCESS"),
                    () -> assertThat(result.data()).isEqualTo(response)
            );
        }

        @Test
        void storeResult_실패_결과를_저장하면_FAILED_상태와_에러_메시지를_반환한다() {
            String jobId = waitingQueue.enqueue(request);

            waitingQueue.storeResult(jobId, JobResult.failed("에러 발생"));

            JobResult result = waitingQueue.getResult(jobId);
            assertAll(
                    () -> assertThat(result.status()).isEqualTo("FAILED"),
                    () -> assertThat(result.errorMessage()).isEqualTo("에러 발생")
            );
        }
    }

    @Nested
    @DisplayName("getResult 테스트")
    class GetResult {

        @Test
        void 등록되지_않은_jobId면_null을_반환한다() {
            JobResult result = waitingQueue.getResult("없는-job-id");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("evictExpiredResults 테스트")
    class EvictExpiredResults {

        @Test
        void 기준일_이전_날짜의_결과는_제거된다() {
            WaitingRequest pastRequest = new WaitingRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            String pastJobId = waitingQueue.enqueue(pastRequest);

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(pastJobId)).isNull();
        }

        @Test
        void 오늘_날짜의_결과는_제거되지_않는다() {
            WaitingRequest todayRequest = new WaitingRequest("유저1", LocalDate.now(), 1L, 1L);
            String todayJobId = waitingQueue.enqueue(todayRequest);

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(todayJobId)).isNotNull();
        }

        @Test
        void 미래_날짜의_결과는_제거되지_않는다() {
            String futureJobId = waitingQueue.enqueue(request);

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(futureJobId)).isNotNull();
        }

        @Test
        void 과거와_미래_결과가_섞여있으면_과거만_제거된다() {
            WaitingRequest pastRequest = new WaitingRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            String pastJobId = waitingQueue.enqueue(pastRequest);
            String futureJobId = waitingQueue.enqueue(request);

            waitingQueue.evictExpiredResults();

            assertAll(
                    () -> assertThat(waitingQueue.getResult(pastJobId)).isNull(),
                    () -> assertThat(waitingQueue.getResult(futureJobId)).isNotNull()
            );
        }
    }
}
