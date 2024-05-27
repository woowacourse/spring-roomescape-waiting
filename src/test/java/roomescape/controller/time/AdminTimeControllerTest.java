package roomescape.controller.time;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class AdminTimeControllerTest {

    @LocalServerPort
    int port;

    String adminToken;
    String memberToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        adminToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("redddy@gmail.com", "0000"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        memberToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("시간 추가")
    void addTimes() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00:00");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약된 시간은 삭제할 수 없다.")
    void deleteTime() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(400);
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

    @ParameterizedTest
    @MethodSource("invalidRequestParameterProvider")
    @DisplayName("유효하지 않은 요청인 경우 400을 반환한다.")
    void invalidRequest(final String startAt) {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("권한이 없는 경우 401을 응답한다.")
    void accessDenied() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00:00");

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(401);
    }
}
