package roomescape.controller.time;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class TimeControllerTest {

    @LocalServerPort
    int port;

    String memberToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        memberToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("타임 조회")
    void getTimes() {
        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void getAvailableTimes() {
        LocalDate date = LocalDate.now().plusDays(10);
        long themeId = 1L;

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/times/availability?date=" + date.format(DateTimeFormatter.ISO_DATE)
                        + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }
}
