package roomescape.member.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.ui.MemberApiFixture;
import roomescape.member.ui.dto.SignUpRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MemberRestControllerTest {

    @Test
    void 회원_생성_요청_성공() {
        final SignUpRequest signUpRequest = MemberApiFixture.signUpRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }
}
