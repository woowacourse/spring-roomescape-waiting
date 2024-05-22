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
import roomescape.member.dto.request.MemberJoinRequest;
import roomescape.member.dto.response.MemberResponse;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.TEST_PASSWORD;

public class MemberAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("관리자 권한으로 사용자 목록을 조회한다.")
    void findAllMembers() {
        // given
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
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
        Member member = createTestMember(MIA_EMAIL);
        String token = createTestToken(member.getEmail().getValue());
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

    @Test
    @DisplayName("동시 요청으로 동일한 email의 사용자를 생성한다.")
    void joinWithDuplicatedEmailInMultiThread() {
        // given
        MemberJoinRequest request = new MemberJoinRequest(MIA_EMAIL, TEST_PASSWORD, MIA_NAME);

        // when
        for (int i = 0; i < 5; i++) {
            new Thread(() -> RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/members/join")
            ).start();
        }

        // then
        List<MemberResponse> members = findAllSavedMembers();
        List<MemberResponse> membersUsingMiaEmail = members.stream()
                .filter(member -> member.email().equals(MIA_EMAIL))
                .toList();
        assertThat(membersUsingMiaEmail).hasSize(1);
    }

    private List<MemberResponse> findAllSavedMembers() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/members")
                .then().log().all()
                .extract();
        return Arrays.stream(response.as(MemberResponse[].class))
                .toList();
    }
}
