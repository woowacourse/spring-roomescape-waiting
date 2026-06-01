package roomescape.reservationtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("특정 날짜/테마의 예약 가능 시간대 조회 API를 테스트합니다.")
    @Test
    void find_available_times() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservationTime(LocalTime.of(11, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", "2028-05-04")
                .queryParam("themeId", themeId)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("startAt", containsInAnyOrder("09:00", "10:00", "11:00"))
                .body("available", everyItem(is(true)));
    }

    @DisplayName("예약 시간을 추가할 수 있어야 한다.")
    @Test
    void save_reservation_time() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "09:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("중복된 시간 추가 시 409를 반환한다.")
    @Test
    void save_duplicate_reservation_time() {
        testHelper.insertReservationTime(LocalTime.of(9, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "09:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @DisplayName("예약 시간을 삭제할 수 있어야 한다.")
    @Test
    void delete_reservation_time() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", timeId)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 시간 삭제 시 404를 반환한다.")
    @Test
    void delete_non_existing_reservation_time() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", 999)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("예약이 존재하는 시간 삭제 시 422를 반환한다.")
    @Test
    void delete_reservation_time_with_existing_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation("타스", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given().log().all()
                .when().delete("/admin/times/{id}", timeId)
                .then().log().all()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
