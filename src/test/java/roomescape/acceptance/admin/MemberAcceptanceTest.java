package roomescape.acceptance.admin;

import static roomescape.acceptance.Fixture.adminToken;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MemberPreviewResponse;

class MemberAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 간소화된 사용자 목록을 조회한다.")
    @Test
    void getAllMemberPreview_success() {
        TypeRef<List<MemberPreviewResponse>> memberListFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(memberListFormat);
    }
}
