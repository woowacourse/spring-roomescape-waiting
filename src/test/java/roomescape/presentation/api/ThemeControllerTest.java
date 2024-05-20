package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.presentation.BaseControllerTest;

class ThemeControllerTest extends BaseControllerTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("모든 테마를 조회하고 성공할 경우 200을 반환한다.")
    void getAllThemes() {
        themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com/image.jpg"));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .extract();

        List<ThemeResponse> themeResponses = response.jsonPath()
                .getList(".", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(themeResponses).hasSize(1);
            softly.assertThat(themeResponses)
                    .containsExactly(new ThemeResponse(1L, "테마 이름", "테마 설명", "https://example.com/image.jpg"));
        });
    }

    @Test
    @DisplayName("인기있는 테마들을 조회하고 성공할 경우 200을 반환한다.")
    @Sql("/popular-themes.sql")
    void getPopularThemes() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .param("startDate", "2024-04-06")
                .param("endDate", "2024-04-10")
                .param("limit", "3")
                .when().get("/themes/popular")
                .then().log().all()
                .extract();

        List<ThemeResponse> popularThemes = response.jsonPath()
                .getList(".", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);

            softly.assertThat(popularThemes.get(0).id()).isEqualTo(4);
            softly.assertThat(popularThemes.get(0).name()).isEqualTo("마법의 숲");
            softly.assertThat(popularThemes.get(0).description()).isEqualTo("요정과 마법사들이 사는 신비로운 숲 속으로!");
            softly.assertThat(popularThemes.get(0).thumbnail()).isEqualTo("https://via.placeholder.com/150/30f9e7");

            softly.assertThat(popularThemes.get(1).id()).isEqualTo(3);
            softly.assertThat(popularThemes.get(1).name()).isEqualTo("시간여행");
            softly.assertThat(popularThemes.get(1).description()).isEqualTo("과거와 미래를 오가며 역사의 비밀을 밝혀보세요.");
            softly.assertThat(popularThemes.get(1).thumbnail()).isEqualTo("https://via.placeholder.com/150/24f355");

            softly.assertThat(popularThemes.get(2).id()).isEqualTo(2);
            softly.assertThat(popularThemes.get(2).name()).isEqualTo("우주 탐험");
            softly.assertThat(popularThemes.get(2).description()).isEqualTo("끝없는 우주에 숨겨진 비밀을 파헤치세요.");
            softly.assertThat(popularThemes.get(2).thumbnail()).isEqualTo("https://via.placeholder.com/150/771796");
        });
    }
}
