package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.result.PaymentReadyResult;
import roomescape.support.SpringBootApiTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApiTest
class PaymentControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

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
}
