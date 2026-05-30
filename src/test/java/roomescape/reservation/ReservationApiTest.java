package roomescape.reservation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationApiTest {

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')"
  })
  void 예약_대기_신청_성공() {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "누누");
    params.put("date", "9999-01-01");
    params.put("timeId", 1);
    params.put("themeId", 1);

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(params)
        .when().post("/reservations")
        .then().log().all()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", is("누누"))
        .body("time.id", is(1))
        .body("status", is("WAITING"));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('일번대기', '9999-01-01', 1, 1, 'WAITING')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('이번대기', '9999-01-01', 1, 1, 'WAITING')"
  })
  void 예약_대기_순번_부여_확인() {
    RestAssured.given().log().all()
        .when().get("/reservations/my?name=일번대기")
        .then().log().all()
        .statusCode(200)
        .body("[0].status", is("WAITING"))
        .body("[0].waitRank", is(1));

    RestAssured.given().log().all()
        .when().get("/reservations/my?name=이번대기")
        .then().log().all()
        .statusCode(200)
        .body("[0].status", is("WAITING"))
        .body("[0].waitRank", is(2));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('누누', '9999-01-01', 1, 1, 'WAITING')"
  })
  void 같은_사용자가_같은_슬롯에_중복_대기할_수_없다() {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "누누");
    params.put("date", "9999-01-01");
    params.put("timeId", 1);
    params.put("themeId", 1);

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(params)
        .when().post("/reservations")
        .then().log().all()
        .statusCode(409);
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('고스트', '9999-01-01', 1, 1, 'WAITING')"
  })
  void 확정_예약_없이_대기만_있는_슬롯에_신청하면_확정_예약이_된다() {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "신규");
    params.put("date", "9999-01-01");
    params.put("timeId", 1);
    params.put("themeId", 1);

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(params)
        .when().post("/reservations")
        .then().log().all()
        .statusCode(201)
        .body("status", is("RESERVED"));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('일번대기', '9999-01-01', 1, 1, 'WAITING')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('이번대기', '9999-01-01', 1, 1, 'WAITING')"
  })
  void 앞_순번_대기가_취소되면_뒤_대기의_순번이_앞당겨진다() {
    int firstWaitingId = RestAssured.given()
        .when().get("/reservations/my?name=일번대기")
        .then().statusCode(200)
        .extract().path("[0].id");

    RestAssured.given().log().all()
        .when().delete("/reservations/my/waiting?name=일번대기&reservationId=" + firstWaitingId)
        .then().log().all()
        .statusCode(200);

    RestAssured.given().log().all()
        .when().get("/reservations/my?name=이번대기")
        .then().log().all()
        .statusCode(200)
        .body("[0].waitRank", is(1));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('선점자', '9999-01-01', 1, 1, 'RESERVED')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('누누', '9999-01-01', 1, 1, 'WAITING')"
  })
  void 사용자는_본인의_대기를_취소할_수_있다() {
    int reservationId = RestAssured.given()
        .when().get("/reservations/my?name=누누")
        .then().statusCode(200)
        .extract().path("[0].id");

    RestAssured.given().log().all()
        .when().delete("/reservations/my/waiting?name=누누&reservationId=" + reservationId)
        .then().log().all()
        .statusCode(200);

    RestAssured.given().log().all()
        .when().get("/reservations/my?name=누누")
        .then().log().all()
        .statusCode(200)
        .body("size()", is(0));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO theme (name, description, image_url) VALUES ('테마', '설명', 'url')",
      "INSERT INTO reservation_time (start_at) VALUES ('10:00')",
      "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES ('누누', '9999-01-01', 1, 1, 'CANCELED')"
  })
  void 취소된_이력이_있는_슬롯에_동일_사용자가_재신청하면_성공한다() {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "누누");
    params.put("date", "9999-01-01");
    params.put("timeId", 1);
    params.put("themeId", 1);

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(params)
        .when().post("/reservations")
        .then().log().all()
        .statusCode(201)
        .body("status", is("RESERVED"));
  }
}
