package roomescape.domain.waiting;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.infra.queue.JobResult;

@ExtendWith(MockitoExtension.class)
class WaitingQueueTest {

    @Mock
    private WaitingService waitingService;

    private WaitingQueue waitingQueue;

    @BeforeEach
    void setUp() {
        waitingQueue = new WaitingQueue(waitingService);
    }

    @Nested
    @DisplayName("enqueue 테스트")
    class Enqueue {

        @Test
        void enqueue_하면_즉시_PENDING_상태로_등록된다() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);

            String jobId = waitingQueue.enqueue(request);

            JobResult result = waitingQueue.getResult(jobId);
            assertAll(
                    () -> assertThat(jobId).isNotNull(),
                    () -> assertThat(result.status()).isEqualTo("PENDING")
            );
        }
    }

    @Nested
    @DisplayName("처리 성공 케이스")
    class ProcessSuccess {

        @Test
        void createWaiting_성공_시_SUCCESS_상태와_응답이_저장된다() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            WaitingResponse response = new WaitingResponse(1L, "유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(waitingService.createWaiting(any())).thenReturn(response);

            String jobId = waitingQueue.enqueue(request);

            await().atMost(1, SECONDS).until(() -> !"PENDING".equals(waitingQueue.getResult(jobId).status()));

            JobResult result = waitingQueue.getResult(jobId);
            assertAll(
                    () -> assertThat(result.status()).isEqualTo("SUCCESS"),
                    () -> assertThat(result.data()).isEqualTo(response),
                    () -> assertThat(result.errorMessage()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("처리 실패 케이스")
    class ProcessFailure {

        @Test
        void RoomescapeException_발생_시_FAILED_상태와_에러_메시지가_저장된다() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(waitingService.createWaiting(any()))
                    .thenThrow(new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME));

            String jobId = waitingQueue.enqueue(request);

            await().atMost(1, SECONDS).until(() -> !"PENDING".equals(waitingQueue.getResult(jobId).status()));

            JobResult result = waitingQueue.getResult(jobId);
            assertAll(
                    () -> assertThat(result.status()).isEqualTo("FAILED"),
                    () -> assertThat(result.errorMessage()).isEqualTo(ErrorCode.DUPLICATE_WAITING_NAME.getMessage()),
                    () -> assertThat(result.data()).isNull()
            );
        }

        @Test
        void 같은_슬롯의_요청은_순서대로_처리된다() {
            WaitingRequest request1 = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            WaitingRequest request2 = new WaitingRequest("유저2", LocalDate.of(2099, 12, 31), 1L, 1L);
            WaitingResponse response1 = new WaitingResponse(1L, "유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            WaitingResponse response2 = new WaitingResponse(2L, "유저2", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(waitingService.createWaiting(request1)).thenReturn(response1);
            when(waitingService.createWaiting(request2)).thenReturn(response2);

            String jobId1 = waitingQueue.enqueue(request1);
            String jobId2 = waitingQueue.enqueue(request2);

            await().atMost(1, SECONDS).until(() ->
                    !"PENDING".equals(waitingQueue.getResult(jobId1).status()) &&
                    !"PENDING".equals(waitingQueue.getResult(jobId2).status())
            );

            assertAll(
                    () -> assertThat(waitingQueue.getResult(jobId1).status()).isEqualTo("SUCCESS"),
                    () -> assertThat(waitingQueue.getResult(jobId2).status()).isEqualTo("SUCCESS")
            );
        }
    }

    @Nested
    @DisplayName("getResult 테스트")
    class GetResult {

        @Test
        void 등록되지_않은_jobId면_null을_반환한다() {
            assertThat(waitingQueue.getResult("없는-job-id")).isNull();
        }
    }

    @Nested
    @DisplayName("evictExpiredResults 테스트")
    class EvictExpiredResults {

        @Test
        void 기준일_이전_날짜의_결과는_제거된다() {
            WaitingRequest pastRequest = new WaitingRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            String pastJobId = waitingQueue.enqueue(pastRequest);
            await().atMost(1, SECONDS).until(() -> !"PENDING".equals(waitingQueue.getResult(pastJobId).status()));

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(pastJobId)).isNull();
        }

        @Test
        void 오늘_날짜의_결과는_제거되지_않는다() {
            WaitingRequest todayRequest = new WaitingRequest("유저1", LocalDate.now(), 1L, 1L);
            String todayJobId = waitingQueue.enqueue(todayRequest);
            await().atMost(1, SECONDS).until(() -> !"PENDING".equals(waitingQueue.getResult(todayJobId).status()));

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(todayJobId)).isNotNull();
        }

        @Test
        void 미래_날짜의_결과는_제거되지_않는다() {
            WaitingRequest futureRequest = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            String futureJobId = waitingQueue.enqueue(futureRequest);
            await().atMost(1, SECONDS).until(() -> !"PENDING".equals(waitingQueue.getResult(futureJobId).status()));

            waitingQueue.evictExpiredResults();

            assertThat(waitingQueue.getResult(futureJobId)).isNotNull();
        }

        @Test
        void 과거와_미래_결과가_섞여있으면_과거만_제거된다() {
            WaitingRequest pastRequest = new WaitingRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            WaitingRequest futureRequest = new WaitingRequest("유저2", LocalDate.of(2099, 12, 31), 1L, 1L);
            String pastJobId = waitingQueue.enqueue(pastRequest);
            String futureJobId = waitingQueue.enqueue(futureRequest);
            await().atMost(1, SECONDS).until(() ->
                    !"PENDING".equals(waitingQueue.getResult(pastJobId).status()) &&
                    !"PENDING".equals(waitingQueue.getResult(futureJobId).status())
            );

            waitingQueue.evictExpiredResults();

            assertAll(
                    () -> assertThat(waitingQueue.getResult(pastJobId)).isNull(),
                    () -> assertThat(waitingQueue.getResult(futureJobId)).isNotNull()
            );
        }
    }
}
