package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createNewReservation;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.request.CreateThemeRequest;
import roomescape.controller.response.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("테마 목록을 조회한다")
    void getAll() {
        // given
        dbHelper.insertTheme(createDefaultTheme());

        // when & then
        List<ThemeResponse> responses = given().log().all()
                .when()
                .get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("테마를 생성한다")
    void create() {
        // given
        CreateThemeRequest request = new CreateThemeRequest(
                "테마 이름",
                "테마 설명",
                "https://example.com/thumbnail.jpg"
        );

        // when & then
        given().log().all()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        Theme saved = themeRepository.findById(1L).get();
        assertThat(saved.getName()).isEqualTo("테마 이름");
        assertThat(saved.getDescription()).isEqualTo("테마 설명");
        assertThat(saved.getThumbnail()).isEqualTo("https://example.com/thumbnail.jpg");
    }

    @Test
    @DisplayName("테마를 삭제한다")
    void delete() {
        // given
        Theme theme = createDefaultTheme();
        dbHelper.insertTheme(theme);

        // when & then
        given().log().all()
                .when()
                .delete("/themes/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(themeRepository.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("테마 랭킹을 조회한다")
    void getRankingTheme() {
        // given
        Theme theme = createDefaultTheme();
        dbHelper.insertTheme(theme);
        dbHelper.insertReservation(createNewReservation(
                createDefaultMember(), LocalDate.now().minusDays(1), createDefaultReservationTime(), theme
        ));

        // when & then
        List<ThemeResponse> responses = given().log().all()
                .when()
                .get("/themes/rank")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertThat(responses).hasSize(1);
    }
} 
