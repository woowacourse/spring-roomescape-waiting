package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.client.TossPaymentException;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.command.PaymentSuccessCommand;
import roomescape.service.dto.result.PaymentConfirmResult;
import roomescape.service.dto.result.PaymentReadyResult;
import roomescape.support.SpringBootApiTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApiTest
class PaymentControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Toss 실호출 없이 결제 흐름을 검증하기 위한 스텁 게이트웨이. 승인 요청을 그대로 성공(DONE) 결과로 되돌린다.
     */
    @TestConfiguration
    static class StubPaymentGatewayConfig {

        @Bean
        @Primary
        PaymentGateway stubPaymentGateway() {
            return confirmation -> {
                // paymentKey 가 "reject" 면 카드 거절을 흉내 내 승인 실패 경로를 검증한다.
                if ("reject".equals(confirmation.paymentKey())) {
                    throw new TossPaymentException.CardRejected("카드가 거절되었습니다");
                }
                return new PaymentResult(
                        confirmation.paymentKey(),
                        confirmation.orderId(),
                        PaymentStatus.DONE,
                        confirmation.amount()
                );
            };
        }
    }

    @Test
    void 주문정보_생성() {
        Long price = 30000L;

        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", price);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");

        Long reservationId = 1L;
        PaymentCreateCommand request = new PaymentCreateCommand(
                reservationId, price
        );

        PaymentReadyResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/payments")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/payments/1")
                .extract().as(PaymentReadyResult.class);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.reservationId()).isEqualTo(reservationId);
        assertThat(response.orderId()).startsWith("order-");
        assertThat(response.amount()).isEqualTo(price);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM payment", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 예약ID가_누락되면_예약시간_생성에_실패한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("price", 30000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/payments")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 금액이_검증되면_결제를_승인한다() {
        Long reservationId = 1L;

        // 샌드박스에서 결제 인증을 마친 후 successUrl에 있는 정보를 입력
        String paymentKey = "test-paymentKey-result";
        String orderId = "order-1";
        Long price = 50000L;

        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", price);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");
        jdbcTemplate.update("INSERT INTO payment (order_id, reservation_id, amount) VALUES (?, ?, ?)", orderId, reservationId, price);

        PaymentSuccessCommand request = new PaymentSuccessCommand(
                orderId, price, paymentKey
        );

        PaymentConfirmResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(200)
                .extract().as(PaymentConfirmResult.class);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.approvedAmount()).isEqualTo(price);
        assertThat(response.paymentKey()).isEqualTo(paymentKey);

        String reservationStatus = jdbcTemplate.queryForObject("SELECT status FROM reservation WHERE id = ?", String.class, reservationId);
        assertThat(reservationStatus).isEqualTo(ReservationStatus.CONFIRMED.name());
        String savedPaymentKey = jdbcTemplate.queryForObject("SELECT payment_key FROM payment WHERE order_id = ?", String.class, orderId);
        assertThat(savedPaymentKey).isEqualTo(paymentKey);
        String paymentStatus = jdbcTemplate.queryForObject("SELECT status FROM payment WHERE order_id = ?", String.class, orderId);
        assertThat(paymentStatus).isEqualTo(PaymentStatus.DONE.name());
    }

    @Test
    void 금액이_불일치하면_결제에_실패한다() {
        Long reservationId = 1L;
        String paymentKey = "test-paymentKey-result";
        String orderId = "order-2";
        Long price = 30000L;
        Long wrongPrice = 100000L;

        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", price);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");
        jdbcTemplate.update("INSERT INTO payment (order_id, reservation_id, amount) VALUES (?, ?, ?)", orderId, reservationId, price);

        PaymentSuccessCommand request = new PaymentSuccessCommand(
                orderId, wrongPrice, paymentKey
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(422);

        String reservationStatus = jdbcTemplate.queryForObject("SELECT status FROM reservation WHERE id = ?", String.class, reservationId);
        assertThat(reservationStatus).isEqualTo(ReservationStatus.PENDING.name());
        String paymentStatus = jdbcTemplate.queryForObject("SELECT status FROM payment WHERE order_id = ?", String.class, orderId);
        assertThat(paymentStatus).isEqualTo(PaymentStatus.READY.name());
    }

    @Test
    void 결제가_실패하면_결제를_삭제처리한다() {
        Long reservationId = 1L;

        String orderId = "order-1";
        Long price = 50000L;

        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", price);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");
        jdbcTemplate.update("INSERT INTO payment (order_id, reservation_id, amount) VALUES (?, ?, ?)", orderId, reservationId, price);

        RestAssured.given().log().all()
                .when().delete("/payments/" + orderId)
                .then().log().all()
                .statusCode(200);

        Integer paymentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment", Integer.class);
        assertThat(paymentCount).isZero();
    }
}
