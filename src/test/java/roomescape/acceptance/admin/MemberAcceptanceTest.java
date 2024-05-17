package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MultipleResponse;

import static roomescape.acceptance.Fixture.adminToken;

class MemberAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 간소화된 사용자 목록을 조회한다.")
    @Test
    void getAllMemberPreview_success() {
        TypeRef<MultipleResponse<MemberPreviewResponse>> membersPreviewFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(membersPreviewFormat);
    }
}
