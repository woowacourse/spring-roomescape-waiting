package roomescape.support.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.support.model.TokenCookieDto;

import java.util.Map;

@Component
public class AuthFixture {

    private final MemberRepository memberRepository;

    public AuthFixture(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public TokenCookieDto saveMemberAndGetJwtTokenCookies(final String email, final String password, final int port) {
        memberRepository.save(new Member("이름", email, password, Role.MEMBER));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }

    public TokenCookieDto saveAdminAndGetTokenCookies(final String email, final String password, final int port) {
        memberRepository.save(new Member("이름", email, password, Role.ADMIN));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }

    public TokenCookieDto loginAndGetTokenCookies(final String email, final String password, int port) {
        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }
}
