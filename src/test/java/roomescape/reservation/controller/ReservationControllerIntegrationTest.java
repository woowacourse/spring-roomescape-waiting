package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
class ReservationControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("예약을 성공적으로 생성한다.")
    void create_Success() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "url");

        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/1")
                .body("id", is(1))
                .body("name", is("브라운"));
    }

    @Test
    @DisplayName("예약 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void create_MissingFields_BadRequest() {
        String requestBody = "{\"name\":\"\", \"date\":\"2026-05-05\", \"timeId\":1, \"themeId\":1}";

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."));
    }

    @Test
    @DisplayName("이름으로 모든 예약을 성공적으로 조회한다.")
    void readAllByName_Success() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "url");
        createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].name", is("브라운"))
                .body("[0].status", is("reserved"));
    }

    @Test
    @DisplayName("이름으로 조회 시 파라미터가 누락되면 400 에러를 반환한다.")
    void readAllByName_MissingName_BadRequest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("필수 요청 파라미터가 누락되었습니다. 입력 값을 다시 확인해 주세요."));
    }
}
