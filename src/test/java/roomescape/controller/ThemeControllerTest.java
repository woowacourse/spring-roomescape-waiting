package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.LoginMember;
import roomescape.dto.member.LoginRequest;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.fixture.LoginMemberFixture;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class ThemeControllerTest {

    private String adminCookie;
    private String userCookie;

    @BeforeEach
    void login() {
        LoginMember admin = LoginMemberFixture.getAdmin();

        adminCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        LoginMember user = LoginMemberFixture.getUser();

        userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @Nested
    @DisplayName("테마 조회")
    class ThemeGetTest {

        @DisplayName("테마 목록을 조회한다")
        @Test
        void themesTest() {
            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("테마 생성")
    class ThemePostTest {

        @DisplayName("어드민은 /themes API를 통해 Theme를 생성할 수 있다")
        @Test
        void addThemeTest() {
            ThemeCreateRequest request = new ThemeCreateRequest("테마", "설명", "https://");

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(2));

            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("일반 유저는 /themes API를 통해 Theme를 생성할 수 없다")
        @Test
        void addThemeExceptionTest1() {
            ThemeCreateRequest request = new ThemeCreateRequest("테마", "설명", "https://");

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(403);
        }

        @DisplayName("기존과 동일한 이름을 가진 테마는 생성할 수 없다")
        @Test
        void addThemeExceptionTest2() {
            ThemeCreateRequest requestTheme = new ThemeCreateRequest("테마 A", "설명", "https://");

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(requestTheme)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("유효하지 않은 name 필드값을 가진 Theme는 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidStrings")
        void invalidRequestThemeTest1(String name) {
            Map<String, String> params = Map.of(
                    "name", name,
                    "description", "hello",
                    "thumbnail", "https://"
            );

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);

        }

        @DisplayName("유효하지 않은 description 필드값을 가진 Theme는 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidStrings")
        void invalidRequestThemeTest2(String description) {
            Map<String, String> params = Map.of(
                    "name", "가이온",
                    "description", description,
                    "thumbnail", "https://"
            );

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);
        }

        static Stream<Arguments> invalidStrings() {
            return Stream.of(
                    Arguments.of(" "),
                    Arguments.of("")
            );
        }

        @DisplayName("유효하지 않은 thumbnail 필드값을 가진 Theme는 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidUrls")
        void invalidRequestThemeTest3(String url) {
            Map<String, String> params = Map.of(
                    "name", "가이온",
                    "description", "hello",
                    "thumbnail", url
            );

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);
        }

        static Stream<Arguments> invalidUrls() {
            return Stream.of(
                    Arguments.of(" "),
                    Arguments.of(""),
                    Arguments.of("ftp://")
            );
        }
    }

    @Nested
    @DisplayName("테마 삭제")
    class ThemeDeleteTest {

        @DisplayName("저장된 테마를 삭제할 수 있다")
        @Test
        void deleteThemeTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("예약된 내역이 존재하는 테마는 삭제할 수 없다")
        @Test
        void deleteThemeExceptionTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(409);
        }

        @DisplayName("일반 유저는 /themes API를 통해 Theme를 삭제할 수 없다")
        @Test
        void deleteThemeExceptionTest2() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(403);
        }

        @DisplayName("존재하지 않는 Id의 Theme 을 삭제할 수 없다")
        @Test
        void invalidThemeIdTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/themes/5")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
