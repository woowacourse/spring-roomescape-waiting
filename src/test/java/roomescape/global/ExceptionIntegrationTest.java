package roomescape.global;

import static org.hamcrest.Matchers.equalTo;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private void setupDefaultTimeAndTheme() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
    }

    @Test
    @DisplayName("예약 날짜가 오늘보다 이전이면 예외가 발생한다.")
    void makeReservation_invalid_date() {
        setupDefaultTimeAndTheme();

        Map<String, Object> body = new HashMap<>();
        body.put("name", "brown");
        body.put("date", LocalDate.now().minusDays(1).toString());
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("기존에 예약이 있으면 예외가 발생한다.")
    void makeReservation_duplicate_reservation() {
        setupDefaultTimeAndTheme();

        Map<String, Object> body = new HashMap<>();
        body.put("name", "brown");
        body.put("date", LocalDate.now().plusDays(7).toString());
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        Map<String, Object> duplicateBody = new HashMap<>(body);
        duplicateBody.put("name", "pobi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(duplicateBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간대입니다. 다른 날짜나 시간을 선택해 주세요."));
    }

    @Test
    @DisplayName("예약 시 name 형식이 유효하지 않으면 예외가 발생한다.")
    void makeReservation_invalid_name_form() {
        Map<String, Object> valid = Map.of(
                "name", "brown",
                "date", LocalDate.now().plusDays(7).toString(),
                "timeId", 1L,
                "themeId", 1L
        );

        Map<String, Object> withNull = new HashMap<>(valid);
        withNull.put("name", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(withNull)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 수정 시, 원본 예약이 이미 지난 예약이면 예외가 발생한다.")
    void updateReservation_expired_original() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        databaseHelper.insertReservationDirectly("brown", LocalDate.now().minusDays(7), 1L, 1L);
        Long id = databaseHelper.findFirstReservationId();

        Map<String, Object> body = new HashMap<>();
        body.put("date", LocalDate.now().plusDays(14).toString());

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/" + id)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("예약 수정 시, 변경하려는 날짜가 이미 지난 날짜이면 예외가 발생한다.")
    void updateReservation_expired_target() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        databaseHelper.insertReservationDirectly("brown", LocalDate.now().plusDays(7), 1L, 1L);
        Long id = databaseHelper.findFirstReservationId();

        Map<String, Object> body = new HashMap<>();
        body.put("date", LocalDate.now().minusDays(1).toString());

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/" + id)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("예약 삭제 시, 이미 지난 예약이면 예외가 발생한다.")
    void deleteReservation_expired() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        databaseHelper.insertReservationDirectly("brown", LocalDate.now().minusDays(7), 1L, 1L);
        Long id = databaseHelper.findFirstReservationId();

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .when().delete("/reservations/" + id)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("예약 수정 시, 변경하려는 예약이 기존 예약과 중복되면 예외가 발생한다.")
    void updateReservation_duplicate() {
        setupDefaultTimeAndTheme();
        Long id = createReservation("brown", LocalDate.now().plusDays(7), 1L, 1L);
        createReservation("pobi", LocalDate.now().plusDays(8), 1L, 1L);

        Map<String, Object> body = new HashMap<>();
        body.put("date", LocalDate.now().plusDays(8).toString());

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/" + id)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("예약 대기 신청 시, 이미 지난 날짜이면 예외가 발생한다.")
    void makeReservationWaiting_expired() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        databaseHelper.insertReservationDirectly("brown", LocalDate.now().minusDays(1), 1L, 1L);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "pobi");
        body.put("date", LocalDate.now().minusDays(1).toString());
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations-waitings")
                .then().log().all()
                .statusCode(422);
    }
}
