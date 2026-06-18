package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.service.PaymentCommandService;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.repository.PaymentOrderRepository;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class PaymentHistoryApiTest {

    private static final String USERNAME = "스타크";

    @Autowired
    private TestDataHelper testHelper;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentCommandService paymentCommandService;

    @MockitoBean
    private PaymentGateway paymentGateway;

    private Long themeId;
    private Long tenTimeId;

    @BeforeEach
    void setUp() {
        themeId = testHelper.insertTheme("공포 테마", "무서운 테마", "https://example.com/theme.jpg");
        tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
    }

    @DisplayName("결제 내역 조회 API는 확정된 결제 주문과 예약 정보를 함께 반환합니다.")
    @Test
    void find_confirmed_payment_history() {
        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.create(confirmedReservationId(), 50_000L));
        testHelper.confirmPaymentOrder(order, "payment-key-confirmed");

        RestAssured.given()
                .queryParam("username", USERNAME)
                .when().get("/payments")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].reservationId", equalTo(order.getReservationId().intValue()))
                .body("[0].name", equalTo(USERNAME))
                .body("[0].date", equalTo("2099-12-31"))
                .body("[0].theme.id", equalTo(themeId.intValue()))
                .body("[0].theme.name", equalTo("공포 테마"))
                .body("[0].time.id", equalTo(tenTimeId.intValue()))
                .body("[0].time.startAt", equalTo("10:00"))
                .body("[0].reservationStatus", equalTo("CONFIRMED"))
                .body("[0].orderId", equalTo(order.getOrderId().value()))
                .body("[0].amount", equalTo(50_000))
                .body("[0].paymentKey", equalTo("payment-key-confirmed"))
                .body("[0].paymentStatus", equalTo("CONFIRMED"));
    }

    @DisplayName("결제 내역 조회 API는 대기 중인 결제 주문의 paymentKey를 null로 반환합니다.")
    @Test
    void find_pending_payment_history() {
        PaymentOrder order = paymentCommandService.prepare(pendingReservationId());

        RestAssured.given()
                .queryParam("username", USERNAME)
                .when().get("/payments")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].reservationStatus", equalTo("PAYMENT_PENDING"))
                .body("[0].orderId", equalTo(order.getOrderId().value()))
                .body("[0].amount", equalTo(50_000))
                .body("[0].paymentKey", nullValue())
                .body("[0].paymentStatus", equalTo("PENDING"));
    }

    @DisplayName("결제 내역 조회 API는 내역이 없으면 빈 배열을 반환합니다.")
    @Test
    void find_empty_payment_history() {
        RestAssured.given()
                .queryParam("username", USERNAME)
                .when().get("/payments")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @DisplayName("결제 내역 조회 API는 이름이 비어 있으면 400을 반환합니다.")
    @Test
    void find_payment_history_with_blank_username() {
        RestAssured.given()
                .queryParam("username", "")
                .when().get("/payments")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("이름은 비어있을 수 없습니다."));
    }

    private Long confirmedReservationId() {
        return testHelper.insertReservation(
                USERNAME,
                LocalDate.of(2099, 12, 31),
                themeId,
                tenTimeId
        );
    }

    private Long pendingReservationId() {
        return testHelper.insertReservation(
                USERNAME,
                LocalDate.of(2099, 12, 31),
                themeId,
                tenTimeId
        );
    }

}
