package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.application.dto.request.SignupRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Role;
import roomescape.presentation.BaseControllerTest;

class MemberControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("회원 가입에 성공할 경우 201을 반환한다.")
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
            softly.assertThat(memberResponse.email()).isEqualTo("new@gmail.com");
            softly.assertThat(memberResponse.name()).isEqualTo("new");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }
}
