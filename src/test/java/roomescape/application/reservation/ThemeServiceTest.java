package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static roomescape.fixture.ThemeFixture.TEST_THEME;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.ThemeResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.fixture.ReservationFixture;

@ServiceTest
@Import(ReservationFixture.class)
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationFixture reservationFixture;

    @Test
    @DisplayName("테마를 생성한다.")
    void shouldReturnCreatedTheme() {
        ThemeRequest request = new ThemeRequest("테마", "테마 설명", "url");
        themeService.create(request);
        List<Theme> themes = themeRepository.findAll();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void shouldReturnAllThemes() {
        themeRepository.save(TEST_THEME.create());
        List<ThemeResponse> themes = themeService.findAll();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("id로 테마를 삭제한다.")
    void shouldDeleteThemeWhenDeleteWithId() {
        Theme theme = themeRepository.save(TEST_THEME.create());
        themeService.deleteById(theme.getId());
        List<Theme> themes = themeRepository.findAll();
        assertThat(themes).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하는 테마를 삭제하는 경우, 예외가 발생한다.")
    void shouldThrowEntityReferenceOnDeleteExceptionWhenDeleteThemeWithReservation() {
        Reservation reservation = reservationFixture.saveReservation();
        long themeId = reservation.getTheme().getId();

        assertThatCode(() -> themeService.deleteById(themeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연관된 예약이 존재하여 삭제할 수 없습니다.");
    }
}
