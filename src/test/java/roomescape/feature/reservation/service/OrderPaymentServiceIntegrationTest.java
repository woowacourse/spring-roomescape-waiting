package roomescape.feature.reservation.service;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.support.ApiFixtures.예약_생성;
import static roomescape.support.ApiFixtures.시간_등록;
import static roomescape.support.ApiFixtures.테마_등록;

import io.restassured.common.mapper.TypeRef;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.feature.payment.domain.Payment;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.repository.PaymentRepository;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.support.IntegrationTestBase;

class OrderPaymentServiceIntegrationTest extends IntegrationTestBase {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    private static final long AMOUNT = 1_000L;

    @Autowired
    private OrderPaymentService orderPaymentService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private Long 예약_id() {
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마", "설명", "https://example.com/image.png");
        return 예약_생성("브라운", FUTURE_DATE, time.id(), theme.id()).id();
    }

    private OrderStatus 주문상태(Long reservationId) {
        return reservationRepository.findReservationByIdAndNotDeleted(reservationId).orElseThrow().getOrderStatus();
    }

    @Test
    void confirm은_주문을_확정하고_결제를_저장한다() {
        Long reservationId = 예약_id();

        orderPaymentService.confirm(reservationId, new PaymentApproveRequest("order-1", "key-1", AMOUNT));

        assertThat(주문상태(reservationId)).isEqualTo(OrderStatus.CONFIRMED);
        List<Payment> payments = paymentRepository.findByReservationIds(List.of(reservationId));
        assertThat(payments).hasSize(1);
        assertThat(payments.getFirst().getOrderId()).isEqualTo("order-1");
        assertThat(payments.getFirst().getPaymentKey()).isEqualTo("key-1");
    }

    @Test
    void markConfirmationRequired는_확인필요_상태로_바꾸고_결제를_저장한다() {
        Long reservationId = 예약_id();

        orderPaymentService.markConfirmationRequired(reservationId, new PaymentApproveRequest("order-2", "key-2", AMOUNT));

        assertThat(주문상태(reservationId)).isEqualTo(OrderStatus.CONFIRMATION_REQUIRED);
        assertThat(paymentRepository.findByReservationIds(List.of(reservationId))).hasSize(1);
    }

    @Test
    void 타임아웃_후_재확인_흐름은_확정되고_결제는_한_건만_남는다() {
        Long reservationId = 예약_id();
        PaymentApproveRequest request = new PaymentApproveRequest("order-3", "key-3", AMOUNT);

        // 타임아웃 → 확인 필요
        orderPaymentService.markConfirmationRequired(reservationId, request);
        // 같은 orderId 로 재확인 성공 → 확정
        orderPaymentService.confirm(reservationId, request);

        assertThat(주문상태(reservationId)).isEqualTo(OrderStatus.CONFIRMED);
        // 같은 orderId 이므로 결제 기록은 한 건만 존재한다.
        assertThat(paymentRepository.findByReservationIds(List.of(reservationId))).hasSize(1);
    }

    @Test
    void markConfirmationRequired를_반복해도_결제는_한_건만_저장된다() {
        Long reservationId = 예약_id();
        PaymentApproveRequest request = new PaymentApproveRequest("order-4", "key-4", AMOUNT);

        orderPaymentService.markConfirmationRequired(reservationId, request);
        orderPaymentService.markConfirmationRequired(reservationId, request);

        assertThat(주문상태(reservationId)).isEqualTo(OrderStatus.CONFIRMATION_REQUIRED);
        assertThat(paymentRepository.findByReservationIds(List.of(reservationId))).hasSize(1);
    }

    @Test
    void 확정된_예약은_조회_시_확정상태와_orderId_paymentKey_금액을_노출한다() {
        Long reservationId = 예약_id();
        orderPaymentService.confirm(reservationId, new PaymentApproveRequest("order-5", "key-5", AMOUNT));

        List<ReservationResponseDto> reservations = given()
                .queryParam("name", "브라운")
                .when().get("/api/reservations")
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {
                });

        assertThat(reservations).hasSize(1);
        ReservationResponseDto dto = reservations.getFirst();
        assertThat(dto.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(dto.orderId()).isEqualTo("order-5");
        assertThat(dto.paymentKey()).isEqualTo("key-5");
        assertThat(dto.amount()).isEqualTo(AMOUNT);
    }
}
