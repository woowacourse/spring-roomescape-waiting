package roomescape.acceptance.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_CUSTOMER_1;

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

    @DisplayName("고객이 로그인 후, 로그인 정보를 확인한다.")
    @TestFactory
    Stream<DynamicTest> login_andGetLoginInfo_success() {
        LogInRequest customerRequest = new LogInRequest(
                PRE_INSERTED_CUSTOMER_1.getEmail(),
                PRE_INSERTED_CUSTOMER_1.getPassword()
        );

        return Stream.of(
                dynamicTest("로그인한다.", () -> {
                            String token = sendLoginRequest(customerRequest);
                            String subject = jwtProvider.getSubject(token);

                            assertThat(subject)
                                    .isEqualTo(PRE_INSERTED_CUSTOMER_1.getId().toString());
                        }
                ),
                dynamicTest("로그인 정보를 확인한다.", () -> {
                            String token = sendLoginRequest(customerRequest);
                            MemberPreviewResponse response = sendCheckNameRequest(token);

                            assertThat(response.name())
                                    .isEqualTo(PRE_INSERTED_CUSTOMER_1.getName());
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

    @DisplayName("고객이 로그아웃 한다.")
    @Test
    void logout_success() {
        LogInRequest adminRequest = new LogInRequest(
                PRE_INSERTED_CUSTOMER_1.getEmail(),
                PRE_INSERTED_CUSTOMER_1.getPassword()
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
