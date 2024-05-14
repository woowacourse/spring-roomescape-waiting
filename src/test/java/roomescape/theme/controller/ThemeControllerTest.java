package roomescape.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberDao memberDao;

    @Test
    @DisplayName("모든 테마 정보를 조회한다.")
    void readThemes() {
        String email = "admin@test.com";
        String password = "12341234";
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin(email, password);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("data.themes.size()", is(0));
    }

    @Test
    @DisplayName("테마를 추가한다.")
    void createThemes() {
        String email = "admin@test.com";
        String password = "12341234";
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin(email, password);

        Map<String, String> params = Map.of(
                "name", "테마명",
                "description", "설명",
                "thumbnail", "http://testsfasdgasd.com"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/themes/1");
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteThemes() {
        String email = "admin@test.com";
        String password = "12341234";
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin(email, password);

        Map<String, String> params = Map.of(
                "name", "테마명",
                "description", "설명",
                "thumbnail", "http://testsfasdgasd.com"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/themes/1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);
    }

    /*
     *  reservationData DataSet ThemeID 별 reservation 개수
     *  5,4,2,5,2,3,1,1,1,1,1
     *  예약 수 내림차순 + ThemeId 오름차순 정렬 순서
     *  1, 4, 2, 6, 3, 5, 7, 8, 9, 10
     */
    @Test
    @DisplayName("예약 수 상위 10개 테마를 조회했을 때 내림차순으로 정렬된다. 만약 예약 수가 같다면, id 순으로 오름차순 정렬된다.")
    @Sql(scripts = {"/truncate.sql", "/reservationData.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void readTop10ThemesDescOrder() {
        LocalDate today = LocalDate.now();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/themes/top?today=" + today)
                .then().log().all()
                .statusCode(200)
                .body("data.themes.size()", is(10))
                .body("data.themes.id", contains(1, 4, 2, 6, 3, 5, 7, 8, 9, 10));
    }

    @ParameterizedTest
    @MethodSource("requestValidateSource")
    @DisplayName("테마 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> invalidRequestBody) {
        String email = "admin@test.com";
        String password = "12341234";
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin(email, password);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminAccessTokenCookie))
                .port(port)
                .body(invalidRequestBody)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> requestValidateSource() {
        return Stream.of(
                Map.of(
                        "name", "테마명",
                        "thumbnail", "http://testsfasdgasd.com"
                ),
                Map.of(
                        "name", "",
                        "description", "설명",
                        "thumbnail", "http://testsfasdgasd.com"
                ),
                Map.of(
                        "name", " ",
                        "description", "설명",
                        "thumbnail", "http://testsfasdgasd.com"
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
