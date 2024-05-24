package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.ADMIN_LOGIN_REQUEST;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.TestFixture;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminThemeControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        themeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("테마를 추가한다.")
    @Test
    void createRoomTheme() {
        // given
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ThemeCreateRequest("themeName", "themeDesc", "thumbnail"))
                .when().post("/admin/themes")
                .then().log().all().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteRoomTheme() {
        // given
        Theme saved = themeRepository.save(new Theme("themeName", "themeDesc", "thumbnail"));

        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/admin/themes/" + saved.getId())
                .then().log().all().statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(themeRepository.count()).isEqualTo(0);
    }
}
