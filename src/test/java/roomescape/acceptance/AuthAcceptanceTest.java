package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.acceptance.fixture.MemberFixture;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.domain.user.Member;

import static roomescape.acceptance.step.MemberStep.멤버_생성;

@AcceptanceTest
public class AuthAcceptanceTest {
    final Member member = MemberFixture.getDomain();

    @Nested
    @DisplayName("이메일,비밀번호,이름을 알맞게 작성한 경우.")
    class ContextWithValidRequest{
        @Test
        @DisplayName("200을 발생한다.")
        void it_returns_200() {
            final MemberCreateRequest request = new MemberCreateRequest(
                    member.getEmail(),
                    member.getPassword(),
                    member.getName()
            );
            RestAssured.given().body(request).contentType(ContentType.JSON)
                    .when().post("/signup")
                    .then().assertThat().statusCode(201);
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
            RestAssured.given().body(request).contentType(ContentType.JSON)
                    .when().post("/signup")
                    .then().assertThat().statusCode(400);
        }
        @Test
        @DisplayName("너무 짧은 비밀번호(8글자 미만)는 400을 발생한다.")
        void it_returns_400_with_too_short_password(){
            final MemberCreateRequest request = new MemberCreateRequest(
                    member.getEmail(),
                    "passwor",
                    member.getName()
            );
            RestAssured.given().body(request).contentType(ContentType.JSON)
                    .when().post("/signup")
                    .then().assertThat().statusCode(400);
        }
    }
    @Nested
    @DisplayName("이미 존재하는 이메일로 회원가입을 시도하는 경우")
    class ContextWithDuplicateEmail {
        @Test
        @DisplayName("409를 발생한다.")
        void it_return_409(){
            final Member createdMember = 멤버_생성();
            final MemberCreateRequest request = new MemberCreateRequest(
                    createdMember.getEmail(),
                    member.getPassword(),
                    member.getName()
            );
            RestAssured.given().body(request).contentType(ContentType.JSON)
                    .when().post("/signup")
                    .then().assertThat().statusCode(409);
        }
    }
}
