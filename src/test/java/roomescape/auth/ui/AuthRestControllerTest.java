package roomescape.auth.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatComparable;
import static roomescape.fixture.ui.MemberApiFixture.signUpRequest1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.ui.dto.CheckAccessTokenResponse;
import roomescape.auth.ui.dto.LoginRequest;
import roomescape.member.ui.dto.SignUpRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthRestControllerTest {

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private void signUp(final SignUpRequest signUpParams) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpParams)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 회원가입한_사용자는_일반_회원_권한을_가진다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        signUp(signUpRequest);
        final LoginRequest loginRequest = new LoginRequest(signUpRequest.email(), signUpRequest.password());

        final Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookies();

        final String accessToken = cookies.get("token");
        final AuthRole authRole = authTokenProvider.getRole(accessToken);

        assertThatComparable(authRole).isEqualTo(AuthRole.MEMBER);
    }

    @Test
    void 로그인_체크_요청_시_회원의_이름을_응답한다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        signUp(signUpRequest);
        final LoginRequest loginRequest = new LoginRequest(signUpRequest.email(), signUpRequest.password());

        final Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookies();

        final CheckAccessTokenResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(CheckAccessTokenResponse.class);

        assertThat(response.name()).isEqualTo(signUpRequest.name());
    }
}
