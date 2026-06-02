package roomescape.reservationtime.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.JdbcReservationSlotRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.test_config.TestClockConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ReservationTimeService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        JdbcReservationSlotRepository.class
})
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationSlotRepository slotRepository;

    @Test
    @DisplayName("이미 존재하는 예약 시간을 생성하면 예외가 발생한다.")
    public void create_fail_duplicate() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        insertReservationTime(startAt);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.create(startAt))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("이미 예약 정보가 존재하는 시간은 삭제할 수 없다.")
    public void delete_fail_hasReservation() {
        // given
        ReservationTime reservationTime = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), reservationTime, theme);

        // when
        assertThatThrownBy(() -> reservationTimeService.delete(reservationTime.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_HAS_RESERVATION.message());
    }

    @Test
    @DisplayName("해당 예약 시간이 존재하지 않으면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void delete_fail_notFound() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationTimeService.delete(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_NOT_FOUND.message());
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.create(name, description, thumbnail));
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        ReservationSlot reservationSlot = slotRepository.upsert(ReservationSlot.create(date, time, theme));
        return reservationRepository.save(Reservation.create(name, reservationSlot, Status.WAITING, LocalDateTime.now()));
    }
}
