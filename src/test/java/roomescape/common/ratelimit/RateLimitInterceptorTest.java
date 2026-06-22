package roomescape.common.ratelimit;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.payment.controller.dto.request.PaymentConfirmRequest;
import roomescape.payment.controller.dto.response.PaymentConfirmResponse;
import roomescape.payment.controller.dto.response.PaymentReadyResponse;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "rate-limit.capacity=2",
                "rate-limit.refill-per-sec=1"
        }
)
class RateLimitInterceptorTest {

    @LocalServerPort
    int port;

    @MockitoBean
    ReservationService reservationService;

    @MockitoBean
    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약과 결제 요청이 한도를 넘으면 컨트롤러 호출 없이 429와 Retry-After를 응답한다")
    void rejectRequestBeforeControllerWhenRateLimitExceeded() {
        when(reservationService.preparePayment(any(ReservationCreateRequest.class)))
                .thenReturn(new PaymentReadyResponse(1L, "order-id", 10000, "방탈출", "브라운", "brown@example.com"));
        when(paymentService.confirm(any(PaymentConfirmRequest.class)))
                .thenReturn(new PaymentConfirmResponse(
                        "order-id",
                        10000,
                        "payment-key",
                        "CONFIRMED",
                        "COMPLETED",
                        "결제가 승인되었습니다."
                ));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationCreateRequest(
                        "브라운",
                        "brown@example.com",
                        LocalDate.now().plusDays(1),
                        1L,
                        1L
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new PaymentConfirmRequest("payment-key", "order-id", 10000))
                .when().post("/payments/confirm")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new PaymentConfirmRequest("payment-key", "order-id", 10000))
                .when().post("/payments/confirm")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", equalTo("1"));

        verify(reservationService, times(1)).preparePayment(any(ReservationCreateRequest.class));
        verify(paymentService, times(1)).confirm(any(PaymentConfirmRequest.class));
    }
}
