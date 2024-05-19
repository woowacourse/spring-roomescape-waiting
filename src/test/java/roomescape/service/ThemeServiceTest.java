package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;

@Sql("/theme-service-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationService reservationService;

    @Test
    void 동일한_이름의_테마를_추가할_경우_예외_발생() {
        //given
        ThemeRequest themeRequest1 = new ThemeRequest("테마명", "테마설명테마설명테마설명", "썸네일명.jpg");
        themeService.addTheme(themeRequest1);

        //when, then
        ThemeRequest themeRequest2 = new ThemeRequest("테마명", "테마설명테마설명테마설명", "썸네일명.jpg");
        assertThatThrownBy(() -> themeService.addTheme(themeRequest2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        List<ThemeResponse> allTheme = themeService.getAllTheme();
        Long notExistIdToFind = allTheme.size() + 1L;

        //when, then
        assertThatThrownBy(() -> themeService.getTheme(notExistIdToFind)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약_되어있는_테마를_삭제할_경우_예외_발생() {
        //given
        ReservationResponse reservationResponse = reservationService.getReservation(1L);
        ThemeResponse themeResponse = reservationResponse.theme();
        Long themeId = themeResponse.id();

        //when, then
        assertThatThrownBy(() -> themeService.deleteTheme(themeId)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 최근_일주일을_기준으로_하여_해당_기간_내에_방문하는_예약이_많은_테마_10개를_확인() {
        //given, when
        List<ThemeResponse> popularThemes = themeService.getPopularThemes();
        List<Long> popularThemeIds = popularThemes.stream()
                .map(ThemeResponse::id)
                .toList();

        //then
        assertAll(
                () -> assertThat(popularThemeIds).hasSize(10),
                () -> assertThat(popularThemeIds.get(0)).isEqualTo(1L),
                () -> assertThat(popularThemeIds.get(1)).isEqualTo(2L),
                () -> assertThat(popularThemeIds).doesNotContain(12L)
        );
    }

    @Test
    void 최근_일주일을_기준으로_하여_해당_기간_내에_방문하는_예약이_많은_테마_10개를_확인_예약이_없는_경우() {
        //given, when
        deleteAllReservation();
        List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        //then
        assertThat(popularThemes).hasSize(0);
    }

    void deleteAllReservation() {
        List<ReservationResponse> reservationResponses = reservationService.getAllReservations();
        reservationResponses.forEach(reservation -> reservationService.deleteReservation(reservation.id()));
    }
}
