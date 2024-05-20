package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpHeaders;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;

public class MemberStep {
    public static Member 멤버_생성(){
        final Member member = MemberFixture.getDomain();
        final MemberCreateRequest request = new MemberCreateRequest(
                member.getEmail(),
                member.getPassword(),
                member.getName()
        );
        RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/signup")
                .then().assertThat().statusCode(201);
        return MemberFixture.getDomain();
    }
    public static String 로그인(final Member member){
        final MemberLoginRequest request = new MemberLoginRequest(
                member.getEmail(),
                member.getPassword()
        );
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/login")
                .then().assertThat().statusCode(200).extract().header(HttpHeaders.SET_COOKIE);
    }

}
