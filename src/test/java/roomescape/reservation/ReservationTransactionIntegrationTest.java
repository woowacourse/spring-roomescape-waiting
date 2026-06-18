package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.Slot;
import roomescape.support.ControllerTestSupport;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingPromotionPolicy;

public class ReservationTransactionIntegrationTest extends ControllerTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private WaitingPromotionPolicy waitingPromotionPolicy;

    @Test
    @DisplayName("대기 승격 중 실패하면 예약 삭제가 롤백된다.")
    void failed_waiting_promotion_rolls_back_reservation_delete() {
        String reservationUserToken = loginUserToken();
        String waitingUserToken = loginWaitingUserToken();

        createWaiting(waitingUserToken);

        given(waitingPromotionPolicy.promote(any(Waiting.class), any(Slot.class)))
                .willThrow(new RuntimeException("승격 실패"));

        RestAssured.given().log().all()
                .header("Authorization", bearer(reservationUserToken))
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(500);

        Integer canceledReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where id = 1",
                Integer.class
        );

        Integer waitingCount = jdbcTemplate.queryForObject(
                "select count(*) from waiting where member_id = 2 and slot_id = 1",
                Integer.class
        );

        Integer promotedReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where member_id = 2 and slot_id = 1",
                Integer.class
        );

        assertThat(canceledReservationCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(1);
        assertThat(promotedReservationCount).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 취소가 먼저 대기를 락으로 잡으면 대기는 승격되고 뒤늦은 대기 취소는 실패한다.")
    void reservation_cancel_first_promotes_waiting_and_late_waiting_cancel_fails() throws Exception {
        String reservationUserToken = loginUserToken();
        String waitingUserToken = loginWaitingUserToken();

        Integer waitingId = createWaiting(waitingUserToken);

        CountDownLatch promotionStarted = new CountDownLatch(1);
        CountDownLatch allowPromotionToFinish = new CountDownLatch(1);
        AtomicBoolean waitingCancelFinishedBeforePromotionRelease = new AtomicBoolean(false);

        given(waitingPromotionPolicy.promote(any(Waiting.class), any(Slot.class)))
                .willAnswer(invocation -> {
                    promotionStarted.countDown();

                    boolean released = allowPromotionToFinish.await(2, TimeUnit.SECONDS);
                    if (!released) {
                        throw new IllegalStateException("테스트 타임아웃: 승격 완료 신호를 받지 못했습니다.");
                    }

                    Waiting waiting = invocation.getArgument(0);
                    Slot slot = invocation.getArgument(1);
                    return Reservation.create(waiting.getMember(), slot);
                });

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<Integer> cancelReservationResult = executorService.submit(() ->
                RestAssured.given().log().all()
                        .header("Authorization", bearer(reservationUserToken))
                        .pathParam("id", 1)
                        .when().delete("/api/user/reservations/{id}")
                        .then().log().all()
                        .extract()
                        .statusCode()
        );

        assertThat(promotionStarted.await(2, TimeUnit.SECONDS)).isTrue();

        Future<Integer> cancelWaitingResult = executorService.submit(() ->
                RestAssured.given().log().all()
                        .header("Authorization", bearer(waitingUserToken))
                        .pathParam("id", waitingId)
                        .when().delete("/api/user/waitings/{id}")
                        .then().log().all()
                        .extract()
                        .statusCode()
        );

        Thread.sleep(200);
        waitingCancelFinishedBeforePromotionRelease.set(cancelWaitingResult.isDone());

        allowPromotionToFinish.countDown();

        assertThat(cancelReservationResult.get(2, TimeUnit.SECONDS)).isEqualTo(204);
        assertThat(cancelWaitingResult.get(2, TimeUnit.SECONDS)).isEqualTo(404);
        assertThat(waitingCancelFinishedBeforePromotionRelease).isFalse();

        Integer promotedReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where member_id = 2 and slot_id = 1",
                Integer.class
        );

        Integer waitingCount = jdbcTemplate.queryForObject(
                "select count(*) from waiting where member_id = 2 and slot_id = 1",
                Integer.class
        );

        assertThat(promotedReservationCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(0);

        executorService.shutdownNow();
    }

    @Test
    @DisplayName("대기 취소가 먼저 대기를 락으로 잡으면 해당 대기는 승격되지 않는다.")
    void waiting_cancel_first_deletes_waiting_and_reservation_cancel_does_not_promote_that_waiting() throws Exception {
        String reservationUserToken = loginUserToken();
        String waitingUserToken = loginWaitingUserToken();

        Integer waitingId = createWaiting(waitingUserToken);

        CountDownLatch waitingLockAcquired = new CountDownLatch(1);
        CountDownLatch allowWaitingDeleteToFinish = new CountDownLatch(1);
        AtomicBoolean reservationCancelFinishedBeforeWaitingRelease = new AtomicBoolean(false);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<Void> waitingCancelTransaction = executorService.submit(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.queryForObject(
                        "select id from waiting where id = ? for update",
                        Long.class,
                        waitingId
                );
                waitingLockAcquired.countDown();

                try {
                    boolean released = allowWaitingDeleteToFinish.await(2, TimeUnit.SECONDS);
                    if (!released) {
                        throw new IllegalStateException("테스트 타임아웃: 대기 삭제 완료 신호를 받지 못했습니다.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }

                jdbcTemplate.update("delete from waiting where id = ?", waitingId);
            });
            return null;
        });

        assertThat(waitingLockAcquired.await(2, TimeUnit.SECONDS)).isTrue();

        Future<Integer> cancelReservationResult = executorService.submit(() ->
                RestAssured.given().log().all()
                        .header("Authorization", bearer(reservationUserToken))
                        .pathParam("id", 1)
                        .when().delete("/api/user/reservations/{id}")
                        .then().log().all()
                        .extract()
                        .statusCode()
        );

        Thread.sleep(200);
        reservationCancelFinishedBeforeWaitingRelease.set(cancelReservationResult.isDone());

        allowWaitingDeleteToFinish.countDown();

        waitingCancelTransaction.get(2, TimeUnit.SECONDS);
        assertThat(cancelReservationResult.get(2, TimeUnit.SECONDS)).isEqualTo(204);
        assertThat(reservationCancelFinishedBeforeWaitingRelease).isFalse();

        Integer promotedReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where member_id = 2 and slot_id = 1",
                Integer.class
        );

        Integer waitingCount = jdbcTemplate.queryForObject(
                "select count(*) from waiting where member_id = 2 and slot_id = 1",
                Integer.class
        );

        Integer canceledReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where id = 1",
                Integer.class
        );

        assertThat(promotedReservationCount).isEqualTo(0);
        assertThat(waitingCount).isEqualTo(0);
        assertThat(canceledReservationCount).isEqualTo(0);

        executorService.shutdownNow();
    }

    private Integer createWaiting(String accessToken) {
        return RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-05-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("data.id");
    }
}
