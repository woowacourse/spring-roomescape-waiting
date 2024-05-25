package roomescape.presentation.api.admin;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.fixture.Fixture;
import roomescape.presentation.BaseControllerTest;

class AdminMemberControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 회원을 조회할 경우 성공하면 200을 반환한다.")
    void getAllMembers() {
        Member admin = memberRepository.save(Fixture.MEMBER_ADMIN);
        String token = tokenProvider.createToken(admin.getId().toString());

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/members")
                .then().log().all()
                .extract();

        List<MemberResponse> memberResponses = response.jsonPath()
                .getList(".", MemberResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(memberResponses).hasSize(1);
            softly.assertThat(memberResponses.get(0)).isEqualTo(MemberResponse.from(admin));
        });
    }
}
