package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.acceptance.fixture.MemberFixture;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.controller.api.dto.response.TokenLoginResponse;
import roomescape.domain.user.Member;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static roomescape.acceptance.step.MemberStep.로그인;
import static roomescape.acceptance.step.MemberStep.멤버_생성;

@AcceptanceTest
public class AuthAcceptanceTest {
    final Member member = MemberFixture.getDomain();

    @Nested
    @DisplayName("회원 가입할 때")
    class DescribeSignup {
        @Nested
        @DisplayName("이메일,비밀번호,이름을 알맞게 작성한 경우.")
        class ContextWithValidRequest {
            @Test
            @DisplayName("200을 반환한다.")
            void it_returns_200() {
                final MemberCreateRequest request = new MemberCreateRequest(
                        member.getEmail(),
                        member.getPassword(),
                        member.getName()
                );

                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/signup")
                        .then().assertThat().statusCode(201);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("이메일,비밀번호,이름을 알맞지 않게 작성한 경우")
        class ContextWithInvalidRequest {
            @Test
            @DisplayName("골뱅이를 포함하지 않은 이메일을 입력시 400을 발생한다.")
            void it_returns_400_with_not_contain_whelk_symbol() {
                final MemberCreateRequest request = new MemberCreateRequest(
                        "joyson5582!gmail.com",
                        member.getPassword(),
                        member.getName()
                );
                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/signup")
                        .then().assertThat().statusCode(400);
                //@formatter:on
            }

            @Test
            @DisplayName("너무 짧은 비밀번호(8글자 미만)는 400을 반환한다.")
            void it_returns_400_with_too_short_password() {
                final MemberCreateRequest request = new MemberCreateRequest(
                        member.getEmail(),
                        "passwor",
                        member.getName()
                );
                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/signup")
                        .then().assertThat().statusCode(400);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("이미 존재하는 이메일로 회원가입을 시도하는 경우")
        class ContextWithDuplicateEmail {
            @Test
            @DisplayName("409를 반환한다.")
            void it_return_409() {
                final Member createdMember = 멤버_생성();
                final MemberCreateRequest request = new MemberCreateRequest(
                        createdMember.getEmail(),
                        member.getPassword(),
                        member.getName()
                );
                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/signup")
                        .then().assertThat().statusCode(409);
                //@formatter:on
            }
        }
    }

    @Nested
    @DisplayName("로그인 할 때")
    class DescribeLogin {
        @Nested
        @DisplayName("DB에 존재하지 않는 이메일로 로그인하는 경우")
        class ContextWithNotExistEmail {
            @Test
            @DisplayName("401을 반환한다.")
            void it_returns_401() {
                final var request = new MemberLoginRequest(
                        member.getEmail(),
                        member.getPassword()
                );
                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/login")
                        .then().assertThat().statusCode(401);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("존재하는 이메일과 비밀번호가 일치하지 않는 경우")
        class ContextWithNotEqualPassword {
            @Test
            @DisplayName("401을 반환한다.")
            void it_returns_401() {
                final Member createdMember = 멤버_생성();

                final var request = new MemberLoginRequest(
                        createdMember.getEmail(),
                        createdMember.getPassword() + "1"
                );
                //@formatter:off
                RestAssured.given().body(request).contentType(ContentType.JSON)
                        .when().post("/login")
                        .then().assertThat().statusCode(401);
                //@formatter:on
            }
        }

        @Nested
        @DisplayName("존재하는 이메일과 비밀번호가 일치하는 경우")
        class ContextWithValidLogin {
            @Test
            @DisplayName("토큰과 200을 반환한다.")
            void it_returns_200_with_token() {

                final var newMember = 멤버_생성();
                final MemberLoginRequest loginRequest = new MemberLoginRequest(
                        newMember.getEmail(),
                        newMember.getPassword()
                );

                //@formatter:off
                final String token = RestAssured.given().body(loginRequest).contentType(ContentType.JSON)
                        .when().post("/login")
                        .then().assertThat().statusCode(200)
                        .extract().header(HttpHeaders.SET_COOKIE);
                //@formatter:on

                assertThat(token).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("로그인 상태를 확인하려 할 때")
    class DescribeLoginCheck {
        @Nested
        @DisplayName("쿠키에 유효한 토큰을 담은 경우")
        class ContextWithToken {
            @Test
            @DisplayName("200과 사용자의 이름을 반환한다.")
            void it_returns_200_with_name() {
                final Member createMember = 멤버_생성();
                final String token = 로그인(createMember);

                //@formatter:off
                final TokenLoginResponse response = RestAssured.given().cookie(token)
                        .when().get("/login/check")
                        .then().assertThat().statusCode(200).extract().as(TokenLoginResponse.class);
                //@formatter:on

                assertThat(response.name()).isEqualTo(createMember.getName());
            }
        }

        @Nested
        @DisplayName("쿠키에 유효하지 않은 토큰을 담은 경우")
        class ContextWithInvalidToken {
            @Test
            @DisplayName("쿠키에 토큰이 없을시 401을 반환한다.")
            void it_returns_401_not_exist_token() {
                //@formatter:off
                RestAssured.given()
                        .when().get("/login/check")
                        .then().assertThat().statusCode(401);
                //@formatter:on
            }
            @Test
            @DisplayName("유효하지 않은 토큰을 넣을시 401을 반환한다.")
            void it_returns_401_with_invalid_token(){
                final String token = "invalidToken";

                //@formatter:off
                RestAssured.given().cookie(token)
                        .when().get("/login/check")
                        .then().assertThat().statusCode(401);
                //@formatter:on
            }
        }
    }
}
