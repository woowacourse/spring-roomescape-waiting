package roomescape.service;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

class ThemeServiceTest {
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;
    private ThemeService themeService;

    @BeforeEach
    void initService() {
        themeRepository = new CollectionThemeRepository();
        reservationRepository = new CollectionReservationRepository();
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    @DisplayName("중복된 테마를 생성할 수 없는지 확인")
    void saveFailWhenDuplicate() {
        themeService.save(ThemeFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> themeService.save(ThemeFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_THEME.getMessage());
    }

    @Test
    @DisplayName("이미 예약이 있는 테마를 지울 수 없는지 확인")
    void deleteFailWhenUsed() {
        Theme saved = themeRepository.save(DEFAULT_THEME);
        reservationRepository.save(ReservationFixture.DEFAULT_RESERVATION);

        Assertions.assertThatThrownBy(() -> themeService.delete(saved.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_THEME.getMessage());
    }
}
