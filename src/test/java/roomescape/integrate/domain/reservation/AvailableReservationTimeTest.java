package roomescape.integrate.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AvailableReservationTimeTest {

    private static String todayDateString;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("drop table reservation");  // 자식 테이블 먼저
        jdbcTemplate.execute("drop table reservation_time");
        jdbcTemplate.execute("drop table theme");
    }

    @BeforeEach
    void setup() {
        todayDateString = LocalDate.now().plusDays(1).toString();
        Map<String, String> timeParam = new HashMap<>();
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        timeParam.put("startAt", afterTime.toString());

        Map<String, String> timeParam2 = new HashMap<>();
        LocalTime afterTime2 = LocalTime.now().plusHours(2L);
        timeParam2.put("startAt", afterTime2.toString());

        Map<String, String> timeParam3 = new HashMap<>();
        LocalTime afterTime3 = LocalTime.now().plusHours(3L);
        timeParam3.put("startAt", afterTime3.toString());

        Map<String, String> themeParam = new HashMap<>();
        themeParam.put("name", "테마 명");
        themeParam.put("description", "description");
        themeParam.put("thumbnail", "thumbnail");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", todayDateString);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam2)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam3)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_가능한_시간을_확인할_수_있다() {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/available-times?date=" + todayDateString + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .extract().response();

        List<Boolean> alreadyBooked = response.jsonPath().getList("alreadyBooked", Boolean.class);
        List<Boolean> booleans = List.of(false, false, true);
        assertThat(alreadyBooked).containsAnyElementsOf(booleans);
    }
}
