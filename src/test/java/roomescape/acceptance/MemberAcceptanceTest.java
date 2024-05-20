package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.dto.ErrorResponse;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberResponse;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class MemberAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("관리자 권한으로 사용자 목록을 조회한다.")
    void findAllMembers() {
        // given
        Member admin = createTestAdmin();
        String token = createTestToken(admin);
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/members")
                .then().log().all()
                .extract();
        List<MemberResponse> memberResponses = Arrays.stream(response.as(MemberResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(memberResponses).isNotNull();
        });
    }

    @Test
    @DisplayName("사용자 권한으로 사용자 목록을 조회한다.")
    void findAllMembersWithoutAuthority() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/members")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusUnauthorized(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }
}
