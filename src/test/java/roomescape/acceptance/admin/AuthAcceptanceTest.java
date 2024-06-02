package roomescape.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_ADMIN;
import static roomescape.util.CookieUtil.TOKEN_NAME;

import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.LoginRequest;
import roomescape.dto.MemberResponse;
import roomescape.util.JwtProvider;

class AuthAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("관리자가 로그인 후, 로그인 정보를 확인한다.")
    @TestFactory
    Stream<DynamicTest> login_andGetLoginInfo_success() {
        LoginRequest adminRequest = new LoginRequest(
                PRE_INSERTED_ADMIN.getEmail(),
                PRE_INSERTED_ADMIN.getPassword()
        );

        return Stream.of(
                dynamicTest("로그인한다.", () -> {
                            String token = sendLoginRequest(adminRequest);
                            long id = jwtProvider.getMemberIdFrom(token);

                            assertThat(id).isEqualTo(PRE_INSERTED_ADMIN.getId());
                        }
                ),
                dynamicTest("로그인 정보를 확인한다.", () -> {
                            String token = sendLoginRequest(adminRequest);
                            MemberResponse response = sendCheckNameRequest(token);

                            assertThat(response.name()).isEqualTo(PRE_INSERTED_ADMIN.getName());
                        }
                )
        );
    }

    private String sendLoginRequest(LoginRequest requestBody) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie(TOKEN_NAME);
    }

    private MemberResponse sendCheckNameRequest(String token) {
        return RestAssured.given().log().all()
                .cookie(TOKEN_NAME, token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(MemberResponse.class);
    }

    @DisplayName("관리자가 로그아웃 한다.")
    @Test
    void logout_success() {
        LoginRequest adminRequest = new LoginRequest(
                PRE_INSERTED_ADMIN.getEmail(),
                PRE_INSERTED_ADMIN.getPassword()
        );
        String token = sendLoginRequest(adminRequest);

        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .cookie(TOKEN_NAME, Matchers.emptyString());
    }
}
