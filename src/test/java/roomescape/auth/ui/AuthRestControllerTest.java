package roomescape.auth.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatComparable;
import static roomescape.fixture.ui.MemberApiFixture.signUpParams1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.ui.dto.CheckAccessTokenResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class AuthRestControllerTest {

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private void signUp(final Map<String, String> signUpParams) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpParams)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 회원가입한_사용자는_일반_회원_권한을_가진다() {
        final Map<String, String> signUpParams = signUpParams1();
        signUp(signUpParams);

        final Map<String, String> loginParams = Map.of(
                "email", signUpParams.get("email"),
                "password", signUpParams.get("password")
        );
        final Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
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
        final Map<String, String> signUpParams = signUpParams1();
        signUp(signUpParams);

        final Map<String, String> loginParams = Map.of(
                "email", signUpParams.get("email"),
                "password", signUpParams.get("password")
        );
        final Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
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

        assertThat(response.name()).isEqualTo(signUpParams.get("name"));
    }
}
