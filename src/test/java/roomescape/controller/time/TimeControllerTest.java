package roomescape.controller.time;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import roomescape.controller.member.dto.MemberLoginRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimeControllerTest {

    @LocalServerPort
    int port;

    String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("redddy@gmail.com", "0000"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("타임 조회")
    void getTimes() {
        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @ParameterizedTest
    @MethodSource("invalidRequestParameterProvider")
    @DisplayName("유효하지 않은 요청인 경우 400을 반환한다.")
    void invalidRequest(final String startAt) {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("시간 삭제")
    void deleteTime() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "00:00");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        final List<Object> values = RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/times/" + values.size())
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/times/" + values.size())
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void getAvailableTimes() {
        LocalDate date = LocalDate.now().plusDays(10);
        long themeId = 1L;

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/times/availability?date=" + date.format(DateTimeFormatter.ISO_DATE) + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    static Stream<Arguments> invalidRequestParameterProvider() {
        return Stream.of(
                Arguments.of("20202020"),
                Arguments.of("1234"),
                Arguments.of("2026-"),
                Arguments.of("2026-13-01"),
                Arguments.of("2026-12-32"),
                Arguments.of("2026-11"),
                Arguments.of("2026-1")
        );
    }
}
