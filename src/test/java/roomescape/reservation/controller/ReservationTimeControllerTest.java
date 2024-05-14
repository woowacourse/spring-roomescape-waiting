package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeControllerTest {

    @Autowired
    private ReservationTimeController reservationTimeController;

    @Autowired
    private MemberDao memberDao;

    @LocalServerPort
    private int port;

    private final Map<String, String> params = Map.of(
            "startAt", "17:00"
    );

    @Test
    @DisplayName("처음으로 등록하는 시간의 id는 1이다.")
    void firstPost() {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/times/1");
    }

    @Test
    @DisplayName("아무 시간도 등록 하지 않은 경우, 시간 목록 조회 결과 개수는 0개이다.")
    void readEmptyTimes() {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.times.size()", is(0));
    }

    @Test
    @DisplayName("하나의 시간만 등록한 경우, 시간 목록 조회 결과 개수는 1개이다.")
    void readTimesSizeAfterFirstPost() {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/times/1");

        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.times.size()", is(1));
    }

    @Test
    @DisplayName("하나의 시간만 등록한 경우, 시간 삭제 뒤 시간 목록 조회 결과 개수는 0개이다.")
    void readTimesSizeAfterPostAndDelete() {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/times/1");

        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.times.size()", is(0));
    }

    @Test
    @DisplayName("컨트롤러에는 JdbcTemplate를 사용한 DB관련 로직이 존재하지 않는다.")
    void jdbcTemplateNotInjected() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationTimeController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }

    @ParameterizedTest
    @MethodSource("validateRequestDataFormatSource")
    @DisplayName("예약 시간 생성 시, 시간 요청 데이터에 시간 포맷이 아닌 값이 입력되어오면 400 에러를 발생한다.")
    void validateRequestDataFormat(Map<String, String> request) {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> validateRequestDataFormatSource() {
        return Stream.of(
                Map.of(
                        "startAt", "24:59"
                ),
                Map.of(
                        "startAt", "hihi")
        );
    }

    @ParameterizedTest
    @MethodSource("validateBlankRequestSource")
    @DisplayName("예약 시간 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> request) {
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("email@email.com", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> validateBlankRequestSource() {
        return Stream.of(
                Map.of(
                ),
                Map.of(
                        "startAt", ""
                ),
                Map.of(
                        "startAt", " "
                )
        );
    }

    private String getAdminAccessTokenCookieByLogin(final String email, final String password) {
        memberDao.insert(new Member("이름", email, password, Role.ADMIN));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        String accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login")
                .then().log().all().extract().cookie("accessToken");

        return "accessToken=" + accessToken;
    }
}
