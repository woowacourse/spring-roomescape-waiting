package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;
import roomescape.dto.theme.command.CreateThemeCommand;
import roomescape.dto.theme.response.ThemeReservationTimeResponse;
import roomescape.dto.theme.response.ThemeResponses;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;

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
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void deleteTheme_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteTheme(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
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
        DbFixtures.insertReservation(jdbcTemplate, "브라운", themeId, Fixtures.daysFromNow(-1).toString(), time1);

        List<ThemeReservationTimeResponse> times =
                service.getThemeTimes(themeId, Fixtures.daysFromNow(-1));

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

        DbFixtures.insertReservation(jdbcTemplate, "a", themeIn, Fixtures.daysFromNow(-7).toString(), timeId);  // 시작 경계
        DbFixtures.insertReservation(jdbcTemplate, "b", themeIn, Fixtures.daysFromNow(-1).toString(), timeId);   // 끝 경계
        DbFixtures.insertReservation(jdbcTemplate, "c", themeOut, Fixtures.daysFromNow(-8).toString(), timeId);  // 시작 직전
        DbFixtures.insertReservation(jdbcTemplate, "d", themeOut, Fixtures.daysFromNow(0).toString(), timeId);  // today

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

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeB, Fixtures.daysFromNow(-5).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeC, Fixtures.daysFromNow(-5).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u3", themeC, Fixtures.daysFromNow(-4).toString(), timeId);

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

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, Fixtures.daysFromNow(-6).toString(), timeId);

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

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, Fixtures.daysFromNow(-6).toString(), timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, Fixtures.daysFromNow(-6).toString(), timeId);

        List<PopularTheme> popular = service.getPopularThemes(2);

        assertThat(popular).hasSize(2);
    }

    @Test
    void getPopularThemes_시드데이터의_랭킹을_집계하고_기간_외_예약은_제외한다() {
        DbFixtures.loadSampleData(jdbcTemplate);

        List<PopularTheme> popular = service.getPopularThemes(10);

        // 기간 내(today-7~today-1) 예약 수: 테마1=10 … 테마10=3 순. 테마13~15의 기간 외 예약은 집계에서 제외된다.
        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        assertThat(popular).extracting(PopularTheme::getReservationCount)
                .containsExactly(10L, 9L, 8L, 7L, 6L, 5L, 4L, 4L, 3L, 3L);
    }
}
