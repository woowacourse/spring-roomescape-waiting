package roomescape.theme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.test_config.TestClockConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ThemeService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class
})
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Test
    @DisplayName("이미 예약 정보가 존재하는 테마는 삭제할 수 없다.")
    public void delete_fail_hasReservation() {
        // given
        ReservationTime reservationTime = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), reservationTime, theme);

        // when
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(THEME_HAS_RESERVATION.message());
    }

    @Test
    @DisplayName("해당 테마가 존재하지 않으면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void delete_fail_notFound() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> themeService.delete(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(THEME_NOT_FOUND.message());
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(Reservation.create(name, date, time, theme, Status.WAITING, LocalDateTime.now()));
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.create(name, description, thumbnail));
    }
}
