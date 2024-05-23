package roomescape.presentation.api.admin;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Role;
import roomescape.presentation.BaseControllerTest;

@Sql("/member.sql")
class AdminMemberControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("모든 회원을 조회할 경우 성공하면 200을 반환한다.")
    void getAllMembers() {
        adminLogin();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/members")
                .then().log().all()
                .extract();

        List<MemberResponse> memberResponses = response.jsonPath()
                .getList(".", MemberResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(memberResponses).hasSize(2);
            softly.assertThat(memberResponses.get(0))
                    .isEqualTo(new MemberResponse(1L, "admin@gmail.com", "어드민", Role.ADMIN));
            softly.assertThat(memberResponses.get(1))
                    .isEqualTo(new MemberResponse(2L, "user@gmail.com", "유저", Role.USER));
        });
    }
}
