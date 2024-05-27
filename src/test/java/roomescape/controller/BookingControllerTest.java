package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.response.TimeSlotResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class BookingControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private int getTotalTimeSlotsCount() {
        List<TimeSlotResponse> timeSlots = RestAssured.given()
                .when().get("/times")
                .then().extract().body()
                .jsonPath().getList("", TimeSlotResponse.class);
        return timeSlots.size();
    }

    @DisplayName("날짜와 테마를 선택하면 예약 가능한 시간들과 200 OK 를 반환한다.")
    @Test
    void given_dateThemeId_when_books_then_statusCodeIsOk() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        RestAssured.given().log().all()
                .when().get("/books?date=" + yesterday + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(getTotalTimeSlotsCount()));
    }
}
