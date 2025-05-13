package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
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
import roomescape.fixture.LoginMemberFixture;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class AuthControllerTest {

    private String adminCookie;
    private String userCookie;

    @BeforeEach
    void setCookies() {
        LoginMember user = LoginMemberFixture.getUser();

        userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        LoginMember admin = LoginMemberFixture.getAdmin();

        adminCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @Nested
    @DisplayName("멤버 목록 조회")
    class MemberGetTest {

        @DisplayName("어드민은 전체 멤버 목록을 조회할 수 있다")
        @Test
        void readMembersTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("일반 유저는 전체 멤버 목록을 조회할 수 없다")
        @Test
        void readMembersExceptionTest() {
            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("회원 가입")
    class MemberPostTest {

        @DisplayName("새 계정을 생성할 수 있다")
        @Test
        void addMemberTest1() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("email", "brown@gmail.com");
            params.put("password", "wooteco7");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("이름이 유효하지 않으면 계정을 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidNames")
        void addMemberExceptionTest1(String name) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            params.put("email", "brown@gmail.com");
            params.put("password", "wooteco7");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        static Stream<Arguments> invalidNames() {
            return Stream.of(
                    Arguments.of(" "),
                    Arguments.of(""),
                    Arguments.of((String) null)
            );
        }

        @DisplayName("이메일이 유효하지 않으면 계정을 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidEmails")
        void addMemberExceptionTest2(String email) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("email", email);
            params.put("password", "wooteco7");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        static Stream<Arguments> invalidEmails() {
            return Stream.of(
                    Arguments.of(" "),
                    Arguments.of(""),
                    Arguments.of((String) null),
                    Arguments.of("asdf")
            );
        }

        @DisplayName("비밀번호가 유효하지 않으면 계정을 생성할 수 없다")
        @ParameterizedTest
        @MethodSource("invalidPasswords")
        void addMemberExceptionTest3(String password) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("email", "brown@gmail.com");
            params.put("password", password);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        static Stream<Arguments> invalidPasswords() {
            return Stream.of(
                    Arguments.of(" "),
                    Arguments.of(""),
                    Arguments.of((String) null),
                    Arguments.of("asdf")
            );
        }

        @DisplayName("동일한 이메일 주소로 중복 가입할 수 없다")
        @Test
        void addMemberExceptionTest4() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("email", "brown@gmail.com");
            params.put("password", "wooteco7");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
