package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createNewReservation;
import static roomescape.TestFixture.createThemeByName;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.dto.request.CreateThemeRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

class ThemeControllerTest extends AbstractRestDocsTest {

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

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
        List<ThemeResponse> responses = givenWithDocs("theme-get")
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
        givenWithDocs("theme-create")
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
        givenWithDocs("theme-delete")
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
        Theme theme1 = createThemeByName("테마1");
        Theme theme2 = createThemeByName("테마2");
        dbHelper.insertTheme(theme1);
        dbHelper.insertTheme(theme2);

        Member member = createDefaultMember();
        dbHelper.insertReservation(createNewReservation(
                member, LocalDate.now().minusDays(1), createDefaultReservationTime(), theme1
        ));

        dbHelper.insertReservation(createNewReservation(
                member, LocalDate.now().minusDays(2), createDefaultReservationTime(), theme1
        ));

        dbHelper.insertReservation(createNewReservation(
                member, LocalDate.now().minusDays(1), createDefaultReservationTime(), theme2
        ));


        // when & then
        List<ThemeResponse> responses = givenWithDocs("theme-rank-get")
                .when()
                .get("/themes/rank")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).name()).isEqualTo("테마1"),
                () -> assertThat(responses.get(1).name()).isEqualTo("테마2")
        );
    }
} 
