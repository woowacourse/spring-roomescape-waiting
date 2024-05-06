package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.member.dto.request.MemberRegisterRequest;

class RoomescapeControllerAdviceTest extends AcceptanceTest {

    @Test
    @DisplayName("권한이 없는 사용자가 admin 페이지에 접근한다.")
    void unAuthorizedMemberTest() {
        MemberRegisterRequest request = new MemberRegisterRequest("name", "email@email.com", "12341234");
        fixture.registerMember(request);
        String token = fixture.loginAndGetToken("email@email.com", "12341234");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }
}
