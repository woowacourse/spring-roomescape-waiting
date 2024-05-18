package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.dto.request.ThemeSaveRequest;
import roomescape.reservation.dto.response.ThemeResponse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.TestFixture.*;

class ThemeAcceptanceTest extends AcceptanceTest {
    @Test
    @DisplayName("[2 - Step2] 테마를 추가한다.")
    void createTheme() {
        // given
        ThemeSaveRequest request = new ThemeSaveRequest(WOOTECO_THEME_NAME, WOOTECO_THEME_DESCRIPTION, THEME_THUMBNAIL);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .extract();
        ThemeResponse themeResponse = response.as(ThemeResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(themeResponse.id()).isNotNull();
            softly.assertThat(themeResponse.name()).isEqualTo(WOOTECO_THEME_NAME);
        });
    }

    @TestFactory
    @DisplayName("[2 - Step2] 테마를 추가하고 삭제한다.")
    Stream<DynamicTest> createThenDeleteTheme() {
        return Stream.of(
                dynamicTest("테마를 하나 생성한다.", this::createTestTheme),
                dynamicTest("테마가 하나 생성된 테마 목록을 조회한다.", () -> findAllThemesWithSize(1)),
                dynamicTest("테마를 하나 삭제한다.", this::deleteOneTheme),
                dynamicTest("테마 목록을 조회한다.", () -> findAllThemesWithSize(0))
        );
    }

    void findAllThemesWithSize(int size) {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .extract();
        List<ThemeResponse> themeResponses = Arrays.stream(response.as(ThemeResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(themeResponses).hasSize(size);
        });
    }

    void deleteOneTheme() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .extract();

        // then
        checkHttpStatusNoContent(response);
    }

    @Test
    @Sql({"/test-schema.sql", "/past-reservation-data.sql"})
    @DisplayName("[2 - Step3] 인기 테마 목록을 조회한다.")
    void findAllPopularThemes() {
        // given
        Long secondRankThemeId = 1L;
        Long firstRankThemeId = 2L;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .extract();
        List<ThemeResponse> themeResponses = Arrays.stream(response.as(ThemeResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(themeResponses).hasSize(2)
                    .extracting(ThemeResponse::id)
                    .containsExactly(firstRankThemeId, secondRankThemeId);
        });
    }
}
