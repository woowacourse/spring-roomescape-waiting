package roomescape.reservationtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ThemeFixture;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class ReservationTimeApiTest {

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("예약 시간 추가 API를 테스트합니다.")
    @Test
    void save_time() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "09:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("startAt", equalTo("09:00"));
    }

    @DisplayName("시간 값 없이 예약 시간 추가 시 400 응답을 반환합니다.")
    @Test
    void save_time_with_null_start_at() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of())
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("시간은 비어있을 수 없습니다."));
    }

    @DisplayName("중복된 시간으로 예약 시간 추가 시 409 응답을 반환합니다.")
    @Test
    void save_duplicated_time() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "09:00"))
                .when().post("/admin/times")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "09:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("시간 09:00이(가) 이미 존재합니다."));
    }

    @DisplayName("예약 시간 삭제 API를 테스트합니다.")
    @Test
    void delete_time() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", timeId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 예약 시간 삭제 시 404 응답을 반환합니다.")
    @Test
    void delete_not_existing_time() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", 999L)
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예약 시간 전체 조회 API를 테스트합니다.")
    @Test
    void find_all_times() {
        testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));

        RestAssured.given().log().all()
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("startAt", containsInAnyOrder("09:00", "10:00"));
    }

    @DisplayName("특정 날짜/테마의 예약 가능 시간대 조회 API를 테스트합니다.")
    @Test
    void find_available_times() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservationTime(LocalTime.of(11, 0));
        LocalDate reservationDate = LocalDate.of(2028, 5, 4);

        testHelper.insertReservation("스타크", reservationDate, themeId, nineTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", reservationDate.toString())
                .queryParam("themeId", themeId)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("startAt", containsInAnyOrder("09:00", "10:00", "11:00"))
                .body("[0].startAt", equalTo("09:00"))
                .body("[0].available", is(false))
                .body("[1].startAt", equalTo("10:00"))
                .body("[1].available", is(true))
                .body("[2].startAt", equalTo("11:00"))
                .body("[2].available", is(true));
    }
}
