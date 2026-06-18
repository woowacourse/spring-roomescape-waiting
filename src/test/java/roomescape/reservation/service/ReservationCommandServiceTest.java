package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.application.dto.PaymentFailCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.repository.PaymentOrderRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
class ReservationCommandServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoSpyBean
    private WaitingRepository waitingRepository;

    @MockitoSpyBean
    private PaymentOrderRepository orderRepository;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("사용자의 방탈출 예약 생성을 테스트합니다.")
    @Test
    void save_user_reservation_successfully() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        ReservationCreateCommand request = ReservationFixture.futureStarkCreateCommand(themeId, timeId, NOW);
        ReservationResult result = reservationCommandService.save(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isPositive();
            softly.assertThat(result.name()).isEqualTo(request.name());
            softly.assertThat(result.date()).isEqualTo(request.date());
            softly.assertThat(result.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(result.time()).isEqualTo(new ReservationTimeResult(timeId, LocalTime.of(10, 0)));
            softly.assertThat(result.status()).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(result.payment()).isNotNull();
            softly.assertThat(result.payment().orderId()).startsWith("order-");
            softly.assertThat(result.payment().amount()).isEqualTo(50_000L);
        });
    }

    @DisplayName("현재 시간보다 이전 시간의 예약 생성 예외를 테스트합니다.")
    @Test
    void save_past_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationCommandService.save(
                ReservationFixture.pastStarkCreateCommand(themeId, timeId, NOW)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("예약 삭제를 테스트합니다.")
    @Test
    void delete_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatNoException().isThrownBy(() -> reservationCommandService.delete(reservationId, NOW));
    }

    @DisplayName("결제 주문이 있는 예약 삭제 시 연결된 주문도 함께 삭제합니다.")
    @Test
    void delete_reservation_with_payment_order() {
        PaymentOrder order = preparePaymentOrder("스타크");
        testHelper.confirmPaymentOrder(order, "confirmed-payment-key");

        reservationCommandService.delete(order.getReservationId(), NOW);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findOptionalPaymentOrderStatus(order.getOrderId().value())).isEmpty();
            softly.assertThat(testHelper.existsReservation(order.getReservationId())).isFalse();
        });
    }

    @DisplayName("삭제할 예약이 없을 시 예외 발생을 테스트합니다.")
    @Test
    void delete_not_found_reservation_exception() {
        assertThatThrownBy(() -> reservationCommandService.delete(1L, NOW))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @DisplayName("삭제할 예약이 현재 시간보다 이전 시간일 경우 예외 발생을 테스트합니다.")
    @Test
    void delete_past_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> reservationCommandService.delete(reservationId, NOW))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("사용자의 방탈출 예약 날짜/시간 변경을 테스트합니다.")
    @Test
    void update_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        ReservationResult result = reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationUpdateDate(), updateTimeId, NOW)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.date()).isEqualTo(ReservationFixture.futureReservationUpdateDate());
            softly.assertThat(result.time().id()).isEqualTo(updateTimeId);
        });
    }

    @DisplayName("동일한 날짜와 시간으로 예약 변경 시 예외 발생을 테스트합니다.")
    @Test
    void update_same_date_and_time_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() -> reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationDate(), timeId, NOW)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("동일한 날짜와 시간으로 변경할 수 없습니다.");
    }

    @DisplayName("존재하지 않는 예약 업데이트 시도 시 예외 발생을 테스트합니다.")
    @Test
    void update_not_found_reservation_exception() {
        Long newTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(
                () -> reservationCommandService.update(1L, ReservationFixture.futureStarkUpdateCommand(newTimeId, NOW)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @DisplayName("변경하려는 예약 날짜에 이미 예약이 존재할 시 예외 발생을 테스트합니다.")
    @Test
    void update_already_reserved_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long starkReservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                tenTimeId
        );

        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        testHelper.insertReservation(
                "카야",
                ReservationFixture.futureReservationUpdateDate(),
                themeId,
                elevenTimeId
        );

        assertThatThrownBy(() -> reservationCommandService.update(
                starkReservationId,
                ReservationFixture.futureStarkUpdateCommand(elevenTimeId, NOW))
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("변경하려는 날짜가 현재 시각보다 이전일 경우 예외 발생을 테스트합니다.")
    @Test
    void update_past_date_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        assertThatThrownBy(() ->
                reservationCommandService.update(
                        reservationId,
                        new ReservationUpdateCommand(ReservationFixture.pastReservationDate(), timeId, NOW)
                ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("이미 지나간 예약 변경 시 예외 발생을 테스트합니다.")
    @Test
    void update_past_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.pastReservationDate(),
                themeId,
                timeId
        );

        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(() ->
                reservationCommandService.update(
                        reservationId,
                        new ReservationUpdateCommand(ReservationFixture.futureReservationUpdateDate(), updateTimeId,
                                NOW)
                ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 변경할 수 없습니다.");
    }

    @DisplayName("이미 확정된 예약이 존재할 경우, 예약 생성 예외를 테스트합니다.")
    @Test
    void save_duplicated_reservation_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        ReservationCreateCommand request = ReservationFixture.futureKayaCreateCommand(themeId, timeId, NOW);

        assertThatThrownBy(() -> reservationCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 날짜와 시간에 예약이 존재합니다.");
    }

    @DisplayName("결제 주문 저장 중 유니크 제약 위반 시 예약 생성 예외를 테스트합니다.")
    @Test
    void save_reservation_payment_order_unique_constraint_exception() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        ReservationCreateCommand request = ReservationFixture.futureStarkCreateCommand(themeId, timeId, NOW);

        doThrow(new UniqueConstraintViolationException(new RuntimeException()))
                .when(orderRepository)
                .save(any());

        assertThatThrownBy(() -> reservationCommandService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("결제 주문 생성에 실패했습니다. 다시 시도해주세요.");

        assertThat(reservationRepository.existsBySlot(request.toSlot(LocalTime.of(10, 0)))).isFalse();
    }

    @DisplayName("확정 예약 삭제 시 예약 대기의 확정 예약으로의 승격을 테스트합니다.")
    @Test
    void delete_confirmed_reservation_and_waiting_to_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        User stark = ReservationFixture.userNameStark();

        reservationCommandService.delete(reservationId, NOW);

        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();
        Reservation promoteReservation = testHelper.findReservationBySlot(slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(promoteReservation.getUser()).isEqualTo(stark);
            softly.assertThat(neoRank).isEqualTo(1);
            softly.assertThatThrownBy(() -> reservationCommandService.delete(reservationId, NOW))
                    .isInstanceOf(NotFoundException.class);
        });
    }

    @DisplayName("확정 예약의 예약 변경 시 대기 중인 예약이 자동 승격 됨을 테스트합니다.")
    @Test
    void update_reservation_and_waiting_to_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(0, 0));
        Long reservationId = testHelper.insertReservation(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        User stark = ReservationFixture.userNameStark();
        User pino = ReservationFixture.userNamePino();

        Long updateTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        reservationCommandService.update(
                reservationId,
                new ReservationUpdateCommand(ReservationFixture.futureReservationUpdateDate(), updateTimeId, NOW)
        );

        ReservationSlot originalSlot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(0, 0))
                .build();
        Reservation promoteReservation = testHelper.findReservationBySlot(originalSlot);

        ReservationSlot updatedSlot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationUpdateDate())
                .themeId(themeId)
                .timeId(updateTimeId)
                .startAt(LocalTime.of(11, 0))
                .build();
        Reservation changeReservation = testHelper.findReservationBySlot(updatedSlot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(changeReservation.getUser()).isEqualTo(pino);
            softly.assertThat(promoteReservation.getUser()).isEqualTo(stark);
        });
    }

    @DisplayName("대기 승격 실패 시 확정 예약 삭제도 함께 롤백된다.")
    @Test
    void reservation_rollback_if_waiting_promote_failed() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피케이",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        doThrow(new RuntimeException("대기 승격 실패"))
                .when(waitingRepository)
                .delete(any());

        assertThatThrownBy(() -> reservationCommandService.delete(reservationId, NOW))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 승격 실패");

        Reservation reservation = testHelper.findReservationBySlot(slot);
        Integer waitingRank = testHelper.findWaitingRank("피케이", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getId()).isEqualTo(reservationId);
            softly.assertThat(reservation.getUser()).isEqualTo(ReservationFixture.userNameStark());
            softly.assertThat(waitingRank).isEqualTo(1);
        });
    }

    @DisplayName("결제 실패 정리는 대기 중인 주문과 연결 예약을 삭제한다.")
    @Test
    void cleanup_pending_payment_failure() {
        PaymentOrder order = preparePaymentOrder("스타크");

        reservationCommandService.cleanupPendingPaymentFailure(paymentFailCommand(order));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findOptionalPaymentOrderStatus(order.getOrderId().value())).isEmpty();
            softly.assertThat(testHelper.existsReservation(order.getReservationId())).isFalse();
        });
    }

    @DisplayName("결제 실패 정리는 대기 중인 주문 예약 삭제 후 첫 번째 대기를 예약으로 승격한다.")
    @Test
    void cleanup_pending_payment_failure_promotes_first_waiting() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "피노",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        PaymentOrder order = orderRepository.save(PaymentOrder.create(reservationId, 50_000L));
        testHelper.insertWaiting("스타크", ReservationFixture.futureReservationDate(), themeId, timeId);
        testHelper.insertWaiting("네오", ReservationFixture.futureReservationDate(), themeId, timeId);
        ReservationSlot slot = ReservationSlot.builder()
                .date(ReservationFixture.futureReservationDate())
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        reservationCommandService.cleanupPendingPaymentFailure(paymentFailCommand(order));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findOptionalPaymentOrderStatus(order.getOrderId().value())).isEmpty();
            softly.assertThat(testHelper.findReservationNameBySlot(slot)).contains("스타크");
            softly.assertThat(testHelper.findWaitingRank("네오", slot)).isEqualTo(1);
        });
    }

    @DisplayName("결제 실패 정리는 이미 확정된 주문과 예약을 삭제하지 않는다.")
    @Test
    void cleanup_confirmed_payment_failure_no_op() {
        PaymentOrder order = preparePaymentOrder("스타크");
        testHelper.confirmPaymentOrder(order, "confirmed-payment-key");

        reservationCommandService.cleanupPendingPaymentFailure(paymentFailCommand(order));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.existsReservation(order.getReservationId())).isTrue();
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("CONFIRMED");
        });
    }

    @DisplayName("결제 실패 정리는 대기 중인 주문이 확정 예약을 가리켜도 예약을 삭제하지 않는다.")
    @Test
    void cleanup_pending_payment_failure_does_not_delete_confirmed_reservation() {
        PaymentOrder order = preparePaymentOrder("스타크");
        testHelper.confirmReservation(order.getReservationId());

        reservationCommandService.cleanupPendingPaymentFailure(paymentFailCommand(order));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.existsReservation(order.getReservationId())).isTrue();
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("CONFIRMED");
        });
    }

    @DisplayName("결제 실패 정리는 알 수 없는 주문 ID에도 아무것도 삭제하지 않는다.")
    @Test
    void cleanup_unknown_payment_failure_no_op() {
        assertThatNoException().isThrownBy(() ->
                reservationCommandService.cleanupPendingPaymentFailure(new PaymentFailCommand("unknown-order-id", NOW))
        );
    }

    private PaymentOrder preparePaymentOrder(String name) {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                name,
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );
        return orderRepository.save(PaymentOrder.create(reservationId, 50_000L));
    }

    private PaymentFailCommand paymentFailCommand(PaymentOrder order) {
        return new PaymentFailCommand(order.getOrderId().value(), NOW);
    }
}
