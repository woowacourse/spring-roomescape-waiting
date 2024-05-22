package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpHeaders;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.controller.api.dto.response.MemberCreateResponse;
import roomescape.controller.api.dto.response.MemberReservationsResponse;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;

public class MemberStep {
    public static MemberCreateResponse 멤버_생성(final String email){
        final Member member = MemberFixture.getDomain(email);
        final MemberCreateRequest request = new MemberCreateRequest(
                member.getEmail(),
                member.getPassword(),
                member.getName()
        );
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/signup")
                .then().assertThat().statusCode(201).extract().as(MemberCreateResponse.class);
    }
    public static MemberCreateResponse 멤버_생성(){
        final Member member = MemberFixture.getDomain();
        final MemberCreateRequest request = new MemberCreateRequest(
                member.getEmail(),
                member.getPassword(),
                member.getName()
        );
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/signup")
                .then().assertThat().statusCode(201).extract().as(MemberCreateResponse.class);
    }
    public static String 로그인(final MemberCreateResponse response){
        final MemberLoginRequest request = new MemberLoginRequest(
                response.email(),
                response.password()
        );
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/login")
                .then().assertThat().statusCode(200).extract().header(HttpHeaders.SET_COOKIE);
    }
    public static String 멤버_생성후_로그인(){
        return 로그인(멤버_생성());
    }
    public static String 이메일로_멤버_생성후_로그인(final String email){
        return 로그인(멤버_생성(email));
    }
    public static MemberReservationsResponse 본인_예약_조회(final String token){
        return RestAssured.given().cookie(token).contentType(ContentType.JSON)
                .when().get("/reservations/mine")
                .then().assertThat().statusCode(200).extract().as(MemberReservationsResponse.class);
    }
    public static String 어드민_로그인(){
        return 로그인(new MemberCreateResponse(1, "admin@email.com","어드민","adminpassword"));
    }
}
