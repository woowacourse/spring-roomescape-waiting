package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(PaymentAcceptanceTest.StubPaymentGatewayConfig.class)
class PaymentAcceptanceTest {

    private static final long PRICE = 30_000L;
    private static final String PAYMENT_KEY = "test_payment_key";

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long themeId;
    private long timeId;
    private final String date = LocalDate.now().plusDays(2).toString();

    /**
     * Toss 실호출 없이 결제 흐름을 검증하기 위한 스텁 게이트웨이. 승인 요청을 그대로 성공(DONE) 결과로 되돌린다.
     */
    @TestConfiguration
    static class StubPaymentGatewayConfig {

        @Bean
        @Primary
        PaymentGateway stubPaymentGateway() {
            return confirmation -> new PaymentResult(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    PaymentStatus.DONE,
                    confirmation.amount()
            );
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                "결제 테스트 테마", "설명", "https://example.com/thumb.jpg", PRICE
        );
        themeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        timeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);

        jdbcTemplate.update("INSERT INTO reservation_date (date) VALUES (?)", date);
        Long dateId = jdbcTemplate.queryForObject("SELECT id FROM reservation_date WHERE date = ?", Long.class, date);
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date_id, time_id, theme_id) VALUES (?, ?, ?)",
                dateId, timeId, themeId
        );
    }

    @Test
    @DisplayName("예약 생성 → 결제 → 승인까지 성공하면 예약이 CONFIRMED 되고 payment_key 가 저장된다")
    void 결제_성공_시_예약이_확정된다() {
        // 1. 예약 생성 (빈 슬롯 → PENDING)
        long reservationId = 예약_생성("브라운");
        assertThat(예약_상태(reservationId)).isEqualTo("PENDING");

        // 2. checkout → 주문(payment row) 생성
        RestAssured.given()
                .when().get("/payments/checkout?reservationId=" + reservationId)
                .then().statusCode(200);

        String orderId = jdbcTemplate.queryForObject(
                "SELECT order_id FROM payment WHERE reservation_id = ?", String.class, reservationId);
        long amount = jdbcTemplate.queryForObject(
                "SELECT amount FROM payment WHERE reservation_id = ?", Long.class, reservationId);
        assertThat(amount).isEqualTo(PRICE);

        // 3. success → confirm (스텁 게이트웨이가 DONE 반환)
        RestAssured.given()
                .when().get("/payments/success?paymentKey=" + PAYMENT_KEY + "&orderId=" + orderId + "&amount=" + amount)
                .then().statusCode(200);

        // 4. 예약 CONFIRMED + payment_key 저장 확인
        assertThat(예약_상태(reservationId)).isEqualTo("CONFIRMED");
        String savedPaymentKey = jdbcTemplate.queryForObject(
                "SELECT payment_key FROM payment WHERE order_id = ?", String.class, orderId);
        assertThat(savedPaymentKey).isEqualTo(PAYMENT_KEY);
    }

    @Test
    @DisplayName("승인 금액이 주문 금액과 다르면 예약은 PENDING 으로 남는다")
    void 금액_불일치_시_예약은_확정되지_않는다() {
        long reservationId = 예약_생성("브라운");
        RestAssured.given()
                .when().get("/payments/checkout?reservationId=" + reservationId)
                .then().statusCode(200);
        String orderId = jdbcTemplate.queryForObject(
                "SELECT order_id FROM payment WHERE reservation_id = ?", String.class, reservationId);

        // 위변조된 금액으로 승인 시도 → fail 뷰
        RestAssured.given()
                .when().get("/payments/success?paymentKey=" + PAYMENT_KEY + "&orderId=" + orderId + "&amount=9999")
                .then().statusCode(200);

        assertThat(예약_상태(reservationId)).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("결제 실패(failUrl)면 결제 대기 예약과 주문이 정리된다")
    void 결제_실패_시_대기_예약과_주문이_정리된다() {
        long reservationId = 예약_생성("브라운");
        RestAssured.given()
                .when().get("/payments/checkout?reservationId=" + reservationId)
                .then().statusCode(200);
        String orderId = jdbcTemplate.queryForObject(
                "SELECT order_id FROM payment WHERE reservation_id = ?", String.class, reservationId);

        RestAssured.given()
                .when().get("/payments/fail?code=PAY_PROCESS_ABORTED&message=결제실패&orderId=" + orderId)
                .then().statusCode(200);

        Long reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Long.class, reservationId);
        Long paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE order_id = ?", Long.class, orderId);
        assertThat(reservationCount).isZero();
        assertThat(paymentCount).isZero();
    }

    @Test
    @DisplayName("orderId 없는 취소(failUrl)도 NPE 없이 처리된다")
    void orderId_없는_취소도_정상_처리된다() {
        RestAssured.given()
                .when().get("/payments/fail?code=PAY_PROCESS_CANCELED&message=사용자취소")
                .then().statusCode(200);
    }

    private long 예약_생성(String name) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/user/reservations")
                .then().statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    private String 예약_상태(long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, reservationId);
    }
}
