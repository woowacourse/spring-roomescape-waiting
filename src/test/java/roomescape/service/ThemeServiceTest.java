package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;
import roomescape.dto.theme.command.CreateThemeCommand;
import roomescape.dto.theme.response.ThemeReservationTimeResponse;
import roomescape.dto.theme.response.ThemeResponses;
import roomescape.exception.ResourceNotFoundException;
import roomescape.fixture.DbFixtures;

class ThemeServiceTest extends ServiceIntegrationTest {

    @Autowired
    private ThemeService service;

    @Test
    void createTheme_id가_채워진_도메인을_반환한다() {
        CreateThemeCommand request = new CreateThemeCommand("공포", "무서움", "https://thumbnail.url");

        Theme created = service.createTheme(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("공포");
        assertThat(created.getDescription()).isEqualTo("무서움");
        assertThat(created.getThumbnailImageUrl()).isEqualTo("https://thumbnail.url");
    }

    @Test
    void getThemes_다음_페이지가_있으면_hasNext가_true() {
        DbFixtures.insertTheme(jdbcTemplate, "A");
        DbFixtures.insertTheme(jdbcTemplate, "B");
        DbFixtures.insertTheme(jdbcTemplate, "C");

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getThemes_다음_페이지가_없으면_hasNext가_false() {
        DbFixtures.insertTheme(jdbcTemplate, "A");
        DbFixtures.insertTheme(jdbcTemplate, "B");

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getTheme_id로_단건을_조회한다() {
        long id = DbFixtures.insertTheme(jdbcTemplate, "공포");

        Theme found = service.getTheme(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("공포");
    }

    @Test
    void getTheme_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.getTheme(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteTheme_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteTheme(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteTheme_삭제후_조회되지_않는다() {
        long id = DbFixtures.insertTheme(jdbcTemplate, "공포");

        service.deleteTheme(id);

        ThemeResponses responses = service.getThemes(0, 10);
        assertThat(responses.themes()).extracting("id").doesNotContain(id);
    }

    @Test
    void getThemeTimes_예약된_시간은_isReserved가_true_나머지는_false() {
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        long time1 = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long time2 = DbFixtures.insertTime(jdbcTemplate, "11:00");
        DbFixtures.insertReservation(jdbcTemplate, "브라운", themeId, "2026-05-06", time1);

        List<ThemeReservationTimeResponse> times =
                service.getThemeTimes(themeId, LocalDate.of(2026, 5, 6));

        assertThat(times).hasSize(2);
        ThemeReservationTimeResponse t1 = times.stream()
                .filter(t -> t.id().equals(time1)).findFirst().orElseThrow();
        ThemeReservationTimeResponse t2 = times.stream()
                .filter(t -> t.id().equals(time2)).findFirst().orElseThrow();
        assertThat(t1.isReserved()).isTrue();
        assertThat(t2.isReserved()).isFalse();
    }

    @Test
    void getPopularThemes_today_minus1_부터_today_minus7_까지의_예약만_집계된다() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long themeIn = DbFixtures.insertTheme(jdbcTemplate, "기간내");
        long themeOut = DbFixtures.insertTheme(jdbcTemplate, "기간외");

        DbFixtures.insertReservation(jdbcTemplate, "a", themeIn, "2026-04-30", timeId);  // 시작 경계
        DbFixtures.insertReservation(jdbcTemplate, "b", themeIn, "2026-05-06", timeId);   // 끝 경계
        DbFixtures.insertReservation(jdbcTemplate, "c", themeOut, "2026-04-29", timeId);  // 시작 직전
        DbFixtures.insertReservation(jdbcTemplate, "d", themeOut, "2026-05-07", timeId);  // today

        List<PopularTheme> popular = service.getPopularThemes(10);

        assertThat(popular).extracting(p -> p.getTheme().getId()).containsExactly(themeIn);
        assertThat(popular).extracting(PopularTheme::getReservationCount).containsExactly(2L);
    }

    @Test
    void getPopularThemes_예약수가_많은_순서대로_정렬되고_집계수를_함께_반환한다() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        long themeC = DbFixtures.insertTheme(jdbcTemplate, "C");

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeB, "2026-05-02", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeC, "2026-05-02", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u3", themeC, "2026-05-03", timeId);

        List<PopularTheme> popular = service.getPopularThemes(10);

        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(themeC, themeB, themeA);
        assertThat(popular).extracting(PopularTheme::getReservationCount)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    void getPopularThemes_예약수가_같으면_id_오름차순으로_정렬된다() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);

        List<PopularTheme> popular = service.getPopularThemes(10);

        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(themeA, themeB);
    }

    @Test
    void getPopularThemes_limit_만큼만_반환한다() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long themeA = DbFixtures.insertTheme(jdbcTemplate, "A");
        long themeB = DbFixtures.insertTheme(jdbcTemplate, "B");
        long themeC = DbFixtures.insertTheme(jdbcTemplate, "C");

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, "2026-05-01", timeId);

        List<PopularTheme> popular = service.getPopularThemes(2);

        assertThat(popular).hasSize(2);
    }
}
