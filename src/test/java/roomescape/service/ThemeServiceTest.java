package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("createTheme - id가 채워진 도메인을 반환한다")
    void createThemeReturnsDomainWithId() {
        CreateThemeCommand request = new CreateThemeCommand("공포", "무서움", "https://thumbnail.url");

        Theme created = service.createTheme(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("공포");
        assertThat(created.getDescription()).isEqualTo("무서움");
        assertThat(created.getThumbnailImageUrl()).isEqualTo("https://thumbnail.url");
    }

    @Test
    @DisplayName("getThemes - 다음 페이지가 있으면 hasNext가 true")
    void getThemesHasNextTrueWhenNextPageExists() {
        DbFixtures.insertTheme(jdbcTemplate, "A");
        DbFixtures.insertTheme(jdbcTemplate, "B");
        DbFixtures.insertTheme(jdbcTemplate, "C");

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getThemes - 다음 페이지가 없으면 hasNext가 false")
    void getThemesHasNextFalseWhenNoNextPage() {
        DbFixtures.insertTheme(jdbcTemplate, "A");
        DbFixtures.insertTheme(jdbcTemplate, "B");

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("getTheme - id로 단건을 조회한다")
    void getThemeFindsSingleById() {
        long id = DbFixtures.insertTheme(jdbcTemplate, "공포");

        Theme found = service.getTheme(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("공포");
    }

    @Test
    @DisplayName("getTheme - 없는 id이면 ResourceNotFoundException")
    void getThemeThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.getTheme(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteTheme - 없는 id이면 ResourceNotFoundException")
    void deleteThemeThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.deleteTheme(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteTheme - 삭제 후 조회되지 않는다")
    void deleteThemeMakesThemeUnqueryable() {
        long id = DbFixtures.insertTheme(jdbcTemplate, "공포");

        service.deleteTheme(id);

        ThemeResponses responses = service.getThemes(0, 10);
        assertThat(responses.themes()).extracting("id").doesNotContain(id);
    }

    @Test
    @DisplayName("getThemeTimes - 예약된 시간은 isReserved가 true, 나머지는 false")
    void getThemeTimesMarksReservedTimes() {
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
    @DisplayName("getPopularThemes - today-1부터 today-7까지의 예약만 집계된다")
    void getPopularThemesAggregatesOnlyLastWeekReservations() {
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
    @DisplayName("getPopularThemes - 예약 수가 많은 순서대로 정렬되고 집계 수를 함께 반환한다")
    void getPopularThemesSortedByReservationCountDescending() {
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
    @DisplayName("getPopularThemes - 예약 수가 같으면 id 오름차순으로 정렬된다")
    void getPopularThemesSortedByIdAscendingWhenCountIsEqual() {
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
    @DisplayName("getPopularThemes - limit 만큼만 반환한다")
    void getPopularThemesReturnsOnlyUpToLimit() {
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
    @DisplayName("getPopularThemes - 시드 데이터의 랭킹을 집계하고 기간 외 예약은 제외한다")
    void getPopularThemesAggregatesSeedDataRankingExcludingOutOfPeriod() {
        DbFixtures.loadSampleData(jdbcTemplate);

        List<PopularTheme> popular = service.getPopularThemes(10);

        // 기간 내(today-7~today-1) 예약 수: 테마1=10 … 테마10=3 순. 테마13~15의 기간 외 예약은 집계에서 제외된다.
        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        assertThat(popular).extracting(PopularTheme::getReservationCount)
                .containsExactly(10L, 9L, 8L, 7L, 6L, 5L, 4L, 4L, 3L, 3L);
    }
}
