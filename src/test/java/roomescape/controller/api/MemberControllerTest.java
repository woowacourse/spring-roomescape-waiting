package roomescape.controller.api;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.controller.BaseControllerTest;
import roomescape.domain.member.Role;
import roomescape.dto.request.SignupRequest;
import roomescape.dto.response.MemberResponse;

class MemberControllerTest extends BaseControllerTest {

    @TestFactory
    @DisplayName("회원 가입, 회원 조회를 한다.")
    Stream<DynamicTest> memberControllerTests() {
        return Stream.of(
                dynamicTest("회원 가입을 한다.", this::signup),
                dynamicTest("모든 회원을 조회한다.", this::getAllMembers)
        );
    }

    void signup() {
        SignupRequest request = new SignupRequest("new@gmail.com", "password", "new");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();

        MemberResponse memberResponse = response.as(MemberResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(response.header("Location")).isEqualTo("/members/" + memberResponse.id());
            softly.assertThat(memberResponse.id()).isNotNull();
            softly.assertThat(memberResponse.name()).isEqualTo("new");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }

    void getAllMembers() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .extract();

        List<MemberResponse> memberResponses = response.jsonPath()
                .getList("list", MemberResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(memberResponses).hasSize(3);
            softly.assertThat(memberResponses.get(2))
                    .isEqualTo(new MemberResponse(3L, "new", Role.USER));
        });
    }
}
