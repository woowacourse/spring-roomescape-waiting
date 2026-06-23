package roomescape.payment;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.payment.exception.CardPaymentRejectedException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(statements = {
        "INSERT INTO store (id, name) VALUES (1, '강남점')",
        "INSERT INTO member (id, email, password, name, role) VALUES (1, 'brown@email.com', 'password', '브라운', 'USER')",
        "INSERT INTO theme (id, name, description, img_url) VALUES (1, '테마', '설명', 'https://example.com/theme.jpg')",
        "INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')"
})
class PaymentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @AfterEach
    void resetPort() {
        RestAssured.port = 8080;
    }

    @Test
    void 결제_인증_전에는_PENDING_예약과_주문정보를_반환한다() {
        String token = authenticate();

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/api/v1/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("reservationStatus", is("PENDING"))
                .body("amount", is(50_000));
    }

    @Test
    void 승인에_성공하면_예약이_CONFIRMED가_된다() {
        String token = authenticate();
        String orderId = prepare(token);
        given(paymentGateway.confirm(any())).willAnswer(invocation -> {
            PaymentConfirmation confirmation = invocation.getArgument(0);
            return new PaymentResult(
                    confirmation.paymentKey(), confirmation.orderId(), PaymentStatus.DONE, confirmation.amount());
        });

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "payment-key", "orderId", orderId, "amount", 50_000))
                .when().post("/api/v1/payments/confirm")
                .then().log().all()
                .statusCode(200)
                .body("status", is("DONE"));

        String reservationStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = 1", String.class);
        org.assertj.core.api.Assertions.assertThat(reservationStatus).isEqualTo("CONFIRMED");
    }

    @Test
    void 조작된_금액은_400이고_게이트웨이를_호출하지_않는다() {
        String token = authenticate();
        String orderId = prepare(token);

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "payment-key", "orderId", orderId, "amount", 49_000))
                .when().post("/api/v1/payments/confirm")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("PAYMENT400_001"));
        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 카드_거절은_사용자에게_의미있는_응답을_준다() {
        String token = authenticate();
        String orderId = prepare(token);
        given(paymentGateway.confirm(any())).willThrow(new CardPaymentRejectedException());

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "payment-key", "orderId", orderId, "amount", 50_000))
                .when().post("/api/v1/payments/confirm")
                .then().log().all()
                .statusCode(422)
                .body("errorCode", is("PAYMENT422_001"));
    }

    @Test
    void failUrl에_orderId가_없어도_204를_반환한다() {
        String token = authenticate();

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("code", "PAY_PROCESS_CANCELED", "message", "사용자가 취소했습니다."))
                .when().post("/api/v1/payments/fail")
                .then().log().all()
                .statusCode(204);
    }

    private String prepare(String token) {
        return RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/api/v1/reservations")
                .then().statusCode(201)
                .extract().path("orderId");
    }

    private Map<String, Object> reservationRequest() {
        return Map.of(
                "date", LocalDate.of(2026, 12, 1).toString(),
                "timeId", 1,
                "themeId", 1,
                "storeId", 1
        );
    }

    private String authenticate() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("brown@email.com", "password"))
                .when().post("/api/v1/auth/login/token")
                .then().statusCode(200)
                .extract().as(TokenResponse.class)
                .token();
    }
}
