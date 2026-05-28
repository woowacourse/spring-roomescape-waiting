package roomescape.global;

import static org.hamcrest.Matchers.equalTo;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
public class ExceptionIntegrationTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private void setupDefaultTimeAndTheme() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
    }

    @Test
    @DisplayName("예약 날짜가 오늘 (5월 1일)보다 이전이면 예외가 발생한다.")
    void makeReservation_invalid_date() {
        setupDefaultTimeAndTheme();

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-04-30");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("예약 날짜가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("예약 시간이 오늘(5월 1일), 이 시간(09:00) 이전이면 예외가 발생한다.")
    void makeReservation_invalid_time() {
        createReservationTime("08:00");
        createTheme("테마", "설명", "thumbnailUrl");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-01");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("시작 시간이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("기존에 예약이 있으면 예외가 발생한다.")
    void makeReservation_duplicate_reservation() {
        setupDefaultTimeAndTheme();

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-01");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약이 이미 존재합니다."));
    }

    @Test
    @DisplayName("예약에 사용 중인 시간을 삭제하면 예외가 발생한다.")
    void delete_time_in_use() {
        setupDefaultTimeAndTheme();

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-01");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 예약 시간에 예약이 존재합니다."));
    }

    @Test
    @DisplayName("예약 시, name에 null이나 공백, 빈 문자열이 들어오면 예외가 발생한다.")
    void makeReservation_invalid_name_form() {
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-05-01",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> WithNull = new HashMap<>(valid);
        WithNull.put("name", null);

        Map<String, Object> WithEmpty = new HashMap<>(valid);
        WithEmpty.put("name", "");

        Map<String, Object> WithWhiteSpace = new HashMap<>(valid);
        WithWhiteSpace.put("name", " ");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(WithNull)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(WithEmpty)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(WithWhiteSpace)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("예약 시, date에 null이나 날짜 형식 아닌 값이 들어오면 예외가 발생한다.")
    void makeReservation_invalid_date_form() {
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-04-29",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> withoutDate = new HashMap<>(valid);
        withoutDate.put("date", null);

        Map<String, Object> withIllegalDateForm = new HashMap<>(valid);
        withIllegalDateForm.put("date", "illegal_form");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutDate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withIllegalDateForm)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("예약 시, timeId, themeId 중 하나라도 null이면 예외가 발생한다.")
    void makeReservation_invalid_timeId_And_themeId_form() {
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-04-29",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> withoutTimeId = new HashMap<>(valid);
        withoutTimeId.put("timeId", null);

        Map<String, Object> withoutThemeId = new HashMap<>(valid);
        withoutThemeId.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutTimeId)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutThemeId)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 요청 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("시간 등록 시, startAt에 null이나 시간 형식 아닌 값이 들어오면 예외가 발생한다.")
    void createTimes_invalid_time_form() {
        Map<String, Object> withoutStartAt = new HashMap<>();
        withoutStartAt.put("startAt", null);

        Map<String, Object> withIllegalStartAt = new HashMap<>();
        withIllegalStartAt.put("startAt", "illegal_format");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutStartAt)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약 시간 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withIllegalStartAt)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("테마 등록 시, name에 null이나 공백, 빈 문자열이 들어오면 예외가 발생한다.")
    void createTheme_invalid_name_form() {
        Map<String, Object> valid = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "thumbnailUrl"
        );

        Map<String, Object> withoutName = new HashMap<>(valid);
        withoutName.put("name", null);

        Map<String, Object> withEmptyName = new HashMap<>(valid);
        withEmptyName.put("name", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutName)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("테마 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withEmptyName)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("테마 요청 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("테마 등록 시, description, thumbnailUrl 중 하나라도 null이면 예외가 발생한다.")
    void createTheme_invalid_description_and_thumbnailUrl_form() {
        Map<String, Object> valid = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "thumbnailUrl"
        );

        Map<String, Object> withoutDescription = new HashMap<>(valid);
        withoutDescription.put("description", null);

        Map<String, Object> withoutThumbnailUrl = new HashMap<>(valid);
        withoutThumbnailUrl.put("thumbnailUrl", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutDescription)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("테마 요청 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withoutThumbnailUrl)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("테마 요청 형식이 유효하지 않습니다."));
    }
}
