package roomescape.waiting.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
class ReservationWaitingControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private void setupDefaultReservation(LocalDate date) {
        createReservationTime("10:00");
        createTheme("테마", "설명", "url");
        createReservation("brown", date, 1L, 1L);
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 생성한다.")
    void create_Success() {
        LocalDate date = LocalDate.now().plusDays(1);
        setupDefaultReservation(date);

        Map<String, Object> request = Map.of(
                "name", "pobi",
                "date", date.toString(),
                "timeId", 1L,
                "themeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations-waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations-waitings/1")
                .body("id", is(1))
                .body("name", is("pobi"));
    }

    @Test
    @DisplayName("예약 대기 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void create_MissingFields_BadRequest() {
        LocalDate date = LocalDate.now().plusDays(1);
        setupDefaultReservation(date);

        Map<String, Object> requestBody = Map.of(
                "name", "",
                "date", date.toString(),
                "timeId", 1L,
                "themeId", 1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations-waitings")
                .then().log().all()
                .statusCode(400)
                .body("message", is("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."));
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 대기 삭제 시 401을 반환한다.")
    void delete_MissingAuthHeader_Unauthorized() {
        LocalDate date = LocalDate.now().plusDays(1);
        setupDefaultReservation(date);

        // 예약 대기 생성
        Map<String, Object> request = Map.of(
                "name", "pobi",
                "date", date.toString(),
                "timeId", 1L,
                "themeId", 1L
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations-waitings")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations-waitings/1")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("예약 대기 삭제 시 본인이 아니면 403을 반환한다.")
    void delete_MismatchOwner_Forbidden() {
        LocalDate date = LocalDate.now().plusDays(1);
        setupDefaultReservation(date);

        // 예약 대기 생성
        Map<String, Object> request = Map.of(
                "name", "pobi",
                "date", date.toString(),
                "timeId", 1L,
                "themeId", 1L
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations-waitings")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "anotherUser")
                .contentType(ContentType.JSON)
                .when().delete("/reservations-waitings/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 삭제한다.")
    void delete_Success() {
        LocalDate date = LocalDate.now().plusDays(1);
        setupDefaultReservation(date);

        // 예약 대기 생성
        Map<String, Object> request = Map.of(
                "name", "pobi",
                "date", date.toString(),
                "timeId", 1L,
                "themeId", 1L
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations-waitings")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "pobi")
                .contentType(ContentType.JSON)
                .when().delete("/reservations-waitings/1")
                .then().log().all()
                .statusCode(204);
    }
}
