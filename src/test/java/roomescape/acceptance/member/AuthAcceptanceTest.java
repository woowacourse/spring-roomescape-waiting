package roomescape.acceptance.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.request.LogInRequest;
import roomescape.dto.response.MemberPreviewResponse;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.acceptance.Fixture.secretKey;
import static roomescape.acceptance.PreInsertedData.CUSTOMER_1;

class AuthAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("고객이 로그인 후, 로그인 정보를 확인한다.")
    @TestFactory
    Stream<DynamicTest> login_andGetLoginInfo_success() {
        LogInRequest customerRequest = new LogInRequest(
                CUSTOMER_1.getEmail(),
                CUSTOMER_1.getPassword()
        );

        return Stream.of(
                dynamicTest("로그인한다.", () -> {
                            String token = sendLoginRequest(customerRequest);
                            Claims claims = parseToken(token);

                            assertThat(claims.getSubject())
                                    .isEqualTo(CUSTOMER_1.getId().toString());
                        }
                ),
                dynamicTest("로그인 정보를 확인한다.", () -> {
                            String token = sendLoginRequest(customerRequest);
                            MemberPreviewResponse response = sendCheckNameRequest(token);

                            assertThat(response.name())
                                    .isEqualTo(CUSTOMER_1.getName());
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

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
                .parseSignedClaims(token)
                .getPayload();
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
                CUSTOMER_1.getEmail(),
                CUSTOMER_1.getPassword()
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
