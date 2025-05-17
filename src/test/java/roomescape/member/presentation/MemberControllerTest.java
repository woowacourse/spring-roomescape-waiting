package roomescape.member.presentation;

import static org.hamcrest.Matchers.greaterThan;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.fixture.MemberFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerTest {
    private final MemberFixture memberFixture = new MemberFixture();

    @Test
    @DisplayName("회원가입 테스트")
    void loginTest() {
        // given
        final SignUpRequest signUpRequest = new SignUpRequest("test@test.com", "test", "테스트");

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/signUp")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    void getMembersTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("비관리자 사용자는 전체 회원 조회가 불가능")
    void getMembersWithNonAdminTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginUser();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/members")
                .then().log().all()
                .statusCode(403);
    }
}
