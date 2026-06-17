package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.service.ReservationFacade;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderIntegrationTest {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')"
  })
  void 빈_슬롯_예약_시_orders_테이블에_레코드가_생성된다() {
    Map<String, Object> params = Map.of(
        "name", "누누", "date", "9999-01-01", "timeId", 1, "themeId", 1);

    int reservationId = RestAssured.given()
        .contentType(ContentType.JSON).body(params)
        .post("/reservations")
        .then().statusCode(201)
        .body("status", is("PENDING"))
        .extract().path("id");

    // DB에서 직접 확인
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM orders WHERE reservation_id = ?", Integer.class, reservationId);
    Long amount = jdbcTemplate.queryForObject(
        "SELECT amount FROM orders WHERE reservation_id = ?", Long.class, reservationId);

    assertThat(count).isEqualTo(1);
    assertThat(amount).isEqualTo(ReservationFacade.DEFAULT_RESERVATION_PRICE);
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')"
  })
  void 슬롯이_이미_차있으면_WAITING으로_등록되고_orders에_저장되지_않는다() {
    Map<String, Object> params = Map.of(
        "name", "누누", "date", "9999-01-01", "timeId", 1, "themeId", 1);

    RestAssured.given()
        .contentType(ContentType.JSON).body(params)
        .post("/reservations")
        .then().statusCode(201).body("status", is("WAITING"));

    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM orders", Integer.class);
    assertThat(count).isZero();
  }
}
