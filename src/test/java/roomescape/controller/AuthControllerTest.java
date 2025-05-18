package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.OK;
import static roomescape.TestFixture.createMemberByName;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.LoginMemberRequest;
import roomescape.controller.response.CheckLoginUserResponse;
import roomescape.controller.response.LoginMemberResponse;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.service.result.MemberResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("로그인을 한다")
    void login() {
        // given
        Member member = Member.createNew("멍구", MemberRole.USER, "test@email.com", "password");
        dbHelper.insertMember(member);

        LoginMemberRequest request = new LoginMemberRequest(
                "test@email.com",
                "password"
        );

        // when & then
        LoginMemberResponse response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/login")
                .then().log().all()
                .statusCode(OK.value())
                .extract()
                .as(LoginMemberResponse.class);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.email()).isEqualTo("test@email.com"),
                () -> assertThat(response.name()).isEqualTo("멍구")
        );
    }

    @Test
    @DisplayName("로그인 상태를 확인한다")
    void loginCheck() {
        // given
        Member member = createMemberByName("멍구");
        dbHelper.insertMember(member);
        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        // when & then
        CheckLoginUserResponse response = given().log().all()
                .cookie("token", token)
                .when()
                .get("/login/check")
                .then().log().all()
                .statusCode(OK.value())
                .extract()
                .as(CheckLoginUserResponse.class);

        assertThat(response.name()).isEqualTo("멍구");
    }

    @Test
    @DisplayName("로그아웃을 한다")
    void logout() {
        // given
        Member member = createMemberByName("멍구");
        dbHelper.insertMember(member);
        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        // when & then
        given().log().all()
                .cookie("token", token)
                .when()
                .post("/logout")
                .then().log().all()
                .statusCode(OK.value());
    }
} 
