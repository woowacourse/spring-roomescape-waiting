package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class ExceptionTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("예약 날짜가 오늘 (5월 1일)보다 이전이면 예외가 발생한다.")
    @Test
    void makeReservation_invalid_date() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-04-30");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("예약 날짜가 유효하지 않습니다."));
    }

    @DisplayName("예약 시간이 오늘(5월 1일), 이 시간(09:00) 이전이면 예외가 발생한다.")
    @Test
    void makeReservation_invalid_time() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(8, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-05-01");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("시작 시간이 유효하지 않습니다."));
    }

    @DisplayName("기존에 예약이 있으면 예외가 발생한다.")
    @Test
    void makeReservation_duplicate_reservation() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-05-01");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약이 이미 존재합니다."));
    }

    @DisplayName("예약에 사용 중인 예외를 삭제하면 예외가 발생한다.")
    @Test
    void delete_time_in_use() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-05-01");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 예약 시간에 예약이 존재합니다."));
    }

    @DisplayName("예약 시, name에 null이나 공백, 빈 문자열이 들어오면 예외가 발생한다.")
    @Test
    void  makeReservation_invalid_name_form() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-05-01",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> paramsWithNull = new HashMap<>(valid);
        paramsWithNull.put("name", null);

        Map<String, Object> paramsWithEmpty = new HashMap<>(valid);
        paramsWithEmpty.put("name", "");

        Map<String, Object> paramsWithWhiteSpace = new HashMap<>(valid);
        paramsWithWhiteSpace.put("name", " ");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithNull)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithEmpty)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithWhiteSpace)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @DisplayName("예약 시, date에 null이나 날짜 형식 아닌 값이 들어오면 예외가 발생한다.")
    @Test
    void makeReservation_invalid_date_form() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-04-29",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> paramsWithoutDate = new HashMap<>(valid);
        paramsWithoutDate.put("date", null);

        Map<String, Object> paramsWithIllegalDateForm = new HashMap<>(valid);
        paramsWithIllegalDateForm.put("date", "illegal_form");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutDate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithIllegalDateForm)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @DisplayName("예약 시, timeId, themeId 중 하나라도 null이면 예외가 발생한다.")
    @Test
    void makeReservation_invalid_timeId_And_themeId_form() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "date", "2026-04-29",
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> paramsWithoutTimeId = new HashMap<>(valid);
        paramsWithoutTimeId.put("timeId", null);

        Map<String, Object> paramsWithoutThemeId = new HashMap<>(valid);
        paramsWithoutThemeId.put("themeId", null);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutTimeId)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutThemeId)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @DisplayName("시간 등록 시, startAt에 null이나 시간 형식 아닌 값이 들어오면 예외가 발생한다.")
    @Test
    void createTimes_invalid_time_form() {
        //given
        Map<String, Object> paramsWithoutStartAt = new HashMap<>();
        paramsWithoutStartAt.put("startAt", null);

        Map<String, Object> paramsWithIllegalStartAt = new HashMap<>();
        paramsWithIllegalStartAt.put("startAt", "illegal_format");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutStartAt)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithIllegalStartAt)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }

    @DisplayName("테마 등록 시, name에 null이나 공백, 빈 문자열이 들어오면 예외가 발생한다.")
    @Test
    void createTheme_invalid_name_form() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "thumbnailUrl"
        );

        Map<String, Object> paramsWithoutName = new HashMap<>(valid);
        paramsWithoutName.put("name", null);

        Map<String, Object> paramsWithEmptyName = new HashMap<>(valid);
        paramsWithEmptyName.put("name", "");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutName)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithEmptyName)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }


    @DisplayName("테마 등록 시, description, thumbnailUrl 중 하나라도 null이면 예외가 발생한다.")
    @Test
    void createTheme_invalid_description_and_thumbnailUrl_form() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "테마",
                "description", "설명",
                "thumbnailUrl", "thumbnailUrl"
        );

        Map<String, Object> paramsWithoutDescription = new HashMap<>(valid);
        paramsWithoutDescription.put("description", null);


        Map<String, Object> paramsWithoutThumbnailUrl = new HashMap<>(valid);
        paramsWithoutThumbnailUrl.put("thumbnailUrl", null);

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutDescription)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(paramsWithoutThumbnailUrl)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 유효하지 않습니다."));
    }
}
