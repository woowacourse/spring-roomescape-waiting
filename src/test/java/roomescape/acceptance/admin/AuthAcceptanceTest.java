package roomescape.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_ADMIN;

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
import roomescape.dto.request.LogInRequest;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.util.JwtProvider;

class AuthAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("관리자가 로그인 후, 로그인 정보를 확인한다.")
    @TestFactory
    Stream<DynamicTest> login_andGetLoginInfo_success() {
        LogInRequest adminRequest = new LogInRequest(
                PRE_INSERTED_ADMIN.getEmail(),
                PRE_INSERTED_ADMIN.getPassword()
        );

        return Stream.of(
                dynamicTest("로그인한다.", () -> {
                            String token = sendLoginRequest(adminRequest);
                            String subject = jwtProvider.getSubject(token);

                            assertThat(subject)
                                    .isEqualTo(PRE_INSERTED_ADMIN.getId().toString());
                        }
                ),
                dynamicTest("로그인 정보를 확인한다.", () -> {
                            String token = sendLoginRequest(adminRequest);
                            MemberPreviewResponse response = sendCheckNameRequest(token);

                            assertThat(response.name())
                                    .isEqualTo(PRE_INSERTED_ADMIN.getName());
                        }
                )
        );
    }

    private String sendLoginRequest(LogInRequest requestBody) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");
    }

    private MemberPreviewResponse sendCheckNameRequest(String token) {
        return RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(MemberPreviewResponse.class);
    }

    @DisplayName("관리자가 로그아웃 한다.")
    @Test
    void logout_success() {
        LogInRequest adminRequest = new LogInRequest(
                PRE_INSERTED_ADMIN.getEmail(),
                PRE_INSERTED_ADMIN.getPassword()
        );
        String token = sendLoginRequest(adminRequest);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .cookie("token", Matchers.emptyString());
    }
}
