package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.AuthInformationResponse;
import roomescape.global.dto.ErrorResponse;
import roomescape.member.domain.Member;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.TEST_PASSWORD;

public class AuthAcceptanceTest extends AcceptanceTest {
    @Test
    @DisplayName("[2 - Step4] 사용자가 로그인한다.")
    void login() {
        // given
        Member member = createTestMember();
        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .extract();
        String token = response.response().getCookie("token");

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(token.split("\\.")).hasSize(3);
        });
    }

    @Test
    @DisplayName("[2 - Step4] 존재하지 않는 이메일로 사용자가 로그인한다.")
    void loginNotExistingMember() {
        // given
        LoginRequest request = new LoginRequest("anonymous@google.com", TEST_PASSWORD);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusUnauthorized(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @Test
    @DisplayName("[2 - Step4] 틀린 비밀번호로 사용자가 로그인한다.")
    void loginWithInvalidPassword() {
        // given
        createTestMember();
        LoginRequest request = new LoginRequest(MIA_EMAIL, "invalid-password");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusUnauthorized(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }


    @Test
    @DisplayName("[2 - Step4] 사용자 인증 정보를 조회한다.")
    void checkAuthInformation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/login/check")
                .then().log().all()
                .extract();
        AuthInformationResponse authInfoResponse = response.as(AuthInformationResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(authInfoResponse.name()).isEqualTo(member.getName());
        });
    }

    @Test
    @DisplayName("[2 - Step4] 유효하지 않은 토큰으로 사용자 인증 정보를 조회한다.")
    void checkAuthInformationWithInvalidToken() {
        // given
        String invalidToken = "invalid-token";
        Cookie cookie = new Cookie.Builder("token", invalidToken).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/login/check")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusUnauthorized(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }
}
