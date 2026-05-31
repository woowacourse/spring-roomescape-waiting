package roomescape;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TimeTest {

  @Test
  void 시간_관리_API() {
    Map<String, String> params = new HashMap<>();
    params.put("startAt", "2030-06-01T10:00");
    params.put("endAt", "2030-06-01T16:00");

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(params)
        .when().post("/times")
        .then().log().all()
        .statusCode(201);

    RestAssured.given().log().all()
        .when().get("/times")
        .then().log().all()
        .statusCode(200)
        .body("size()", is(1));

    RestAssured.given().log().all()
        .when().delete("/times/1")
        .then().log().all()
        .statusCode(204);
  }

  @Test
  void 없는_시간_삭제시_404_에러_응답() {
    RestAssured.given().log().all()
        .when().delete("/times/999")
        .then().log().all()
        .statusCode(404)
        .body("code", is("TIME_NOT_FOUND"))
        .body("message", is("예약 시간을 찾을 수 없습니다."));
  }

  @Test
  void 예약과_시간_연결() {
    Map<String, String> theme = new HashMap<>();
    theme.put("name", "테마");
    theme.put("description", "설명");
    theme.put("imageUrl", "https://example.com/theme.png");

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(theme)
        .when().post("/themes")
        .then().log().all()
        .statusCode(201);

    Map<String, String> time = new HashMap<>();
    time.put("startAt", "2030-06-01T10:00");
    time.put("endAt", "2030-06-01T16:00");

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(time)
        .when().post("/times")
        .then().log().all()
        .statusCode(201);

    Map<String, Object> reservation = new HashMap<>();
    reservation.put("name", "브라운");
    reservation.put("themeId", 1);
    reservation.put("timeId", 1);

    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(reservation)
        .when().post("/admin/reservations")
        .then().log().all()
        .statusCode(201);

    RestAssured.given().log().all()
        .when().get("/admin/reservations")
        .then().log().all()
        .statusCode(200)
        .body("size()", is(1));
  }
}
