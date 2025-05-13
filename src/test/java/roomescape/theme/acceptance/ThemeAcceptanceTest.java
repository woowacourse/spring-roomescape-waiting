package roomescape.theme.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.theme.dto.request.ThemeRequest.ThemeCreateRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(0L, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );

        // when & then
        TestHelper.postWithToken("/themes", request, token)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1))
                .body("name", equalTo("미소"))
                .body("description", equalTo("미소 테마"))
                .body("thumbnail", equalTo("https://miso.com"));
    }

    @Test
    @DisplayName("중복되는 테마 이름이 있을 경우 생성할 수 없다.")
    void createThemeWithDuplicateName() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "미소",
                "미소 테마2",
                "https://miso2.com"
        );

        TestHelper.postWithToken("/themes", request1, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        TestHelper.postWithToken("/themes", request2, token)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("이미 존재하는 테마 이름입니다."));
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void getAllThemes() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );

        TestHelper.postWithToken("/themes", request1, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        TestHelper.postWithToken("/themes", request2, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        TestHelper.get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].name", equalTo("미소"))
                .body("[1].name", equalTo("우테코"));
    }

    @Test
    @DisplayName("인기 있는 테마를 조회한다.")
    void getPopularThemes() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );

        TestHelper.postWithToken("/themes", request1, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        TestHelper.postWithToken("/themes", request2, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        TestHelper.get("/themes/popular?limit=2")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteTheme() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );

        TestHelper.postWithToken("/themes", request, token)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        TestHelper.deleteWithToken("/themes/1", token)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        TestHelper.get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 예외가 발생한다.")
    void deleteNonExistentTheme() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.deleteWithToken("/themes/1", token)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("존재하지 않는 테마입니다."));
    }
}
