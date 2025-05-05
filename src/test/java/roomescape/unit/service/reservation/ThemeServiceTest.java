package roomescape.unit.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Theme;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.AddReservationTimeDto;
import roomescape.dto.reservation.AddThemeDto;
import roomescape.repository.reservation.ThemeRepository;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.ReservationTimeService;
import roomescape.service.reservation.ThemeService;
import roomescape.unit.repository.reservation.FakeReservationRepository;
import roomescape.unit.repository.reservation.FakeReservationTimeRepository;
import roomescape.unit.repository.reservation.FakeThemeRepository;

class ThemeServiceTest {

    private ThemeService themeService;
    private ThemeRepository themeRepository;
    private ReservationService reservationService;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        FakeReservationTimeRepository fakeReservationTimeRepository = new FakeReservationTimeRepository();
        FakeReservationRepository fakeReservationRepository = new FakeReservationRepository();

        reservationTimeService = new ReservationTimeService(fakeReservationRepository, fakeReservationTimeRepository);
        reservationService = new ReservationService(fakeReservationRepository,
                fakeReservationTimeRepository,
                themeRepository);
        themeService = new ThemeService(themeRepository, fakeReservationRepository);
    }

    @Test
    void 테마를_추가할_수_있다() {
        AddThemeDto addThemeDto = new AddThemeDto("방탈출", "게임입니다.", "thumbnail");
        long id = themeService.addTheme(addThemeDto);

        assertThat(id).isEqualTo(1L);
    }

    @Test
    void 테마를_조회할_수_있다() {
        Theme theme = new Theme(null, "방탈출", "게임입니다.", "thumbnail");
        themeRepository.add(theme);

        assertThat(themeService.findAll()).hasSize(1);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        Theme theme = new Theme(null, "방탈출", "게임입니다.", "thumbnail");
        themeRepository.add(theme);
        themeService.deleteThemeById(1L);
        assertThat(themeRepository.findAll()).hasSize(0);
    }

    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        Theme theme = new Theme(null, "방탈출", "게임입니다.", "thumbnail");
        themeRepository.add(theme);

        Long timeId = reservationTimeService.addReservationTime(new AddReservationTimeDto(LocalTime.now()));
        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1L), timeId, 1L));
        assertThatThrownBy(() -> themeService.deleteThemeById(1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_테마를_조회시_예외가_발생한다() {
        assertThatThrownBy(() -> themeService.getThemeById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
