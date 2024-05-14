package roomescape.controller.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.service.MemberService;
import roomescape.util.TokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminPageControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    MemberService memberService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("/admin/theme 요청시 테마 관리 페이지를 응답한다.")
    void response_theme_page() {
        final var output = memberService.createMember(MemberFixture.getAdminCreateInput());
        final String token = tokenProvider.generateToken(Member.fromMember(output.id(), output.name(), output.email(), output.password()));
        RestAssured.given().contentType(ContentType.JSON).cookie("token", token)
                .when().get("/admin/theme")
                .then().statusCode(200);
    }

}
