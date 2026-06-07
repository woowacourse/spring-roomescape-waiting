package roomescape.reservationtime.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    public void create_success() {
        // when
        ReservationTime reservationTime = reservationTimeService.create(LocalTime.of(10, 0));

        // then
        assertThat(reservationTimeService.findAll()).containsExactly(reservationTime);
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간을 생성하면 예외가 발생한다.")
    public void create_fail() {
        // given
        LocalTime startAt = LocalTime.of(23, 59);
        reservationTimeService.create(startAt);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.create(startAt))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("예약 시간을 비활성화한다.")
    public void deactivate_success() {
        // given
        ReservationTime reservationTime = reservationTimeService.create(LocalTime.of(10, 0));

        // when
        reservationTimeService.deactivate(reservationTime.getId());

        // then
        assertThat(reservationTimeService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간도 비활성화한다.")
    public void deactivate_success_whenReservationExists() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        saveReservation("브라운", LocalDate.of(2026, 5, 14), reservationTime, theme);

        // when
        reservationTimeService.deactivate(reservationTime.getId());

        // then
        assertThat(reservationTimeService.findAll()).isEmpty();
        assertThat(reservationTimeRepository.findById(reservationTime.getId())).isEmpty();
    }

    @Test
    @DisplayName("비활성화된 예약 시간으로 예약을 생성하면 예외가 발생한다.")
    public void createReservation_fail_whenReservationTimeInactive() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        reservationTimeService.deactivate(reservationTime.getId());

        // when, then
        assertThatThrownBy(() -> reservationService.create(
                "브라운",
                LocalDate.of(2026, 5, 14),
                reservationTime.getId(),
                theme.getId()
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 비활성화를 요청해도 성공한다.")
    public void deactivate_success_whenReservationTimeNotFound() {
        // when
        reservationTimeService.deactivate(37L);

        // then
        assertThat(reservationTimeService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("특정 날짜 및 테마의 예약 가능한 시간들을 반환한다.")
    public void findAvailableTimes_success() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(12, 0)));
        Theme targetTheme = themeRepository.save(Theme.create("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        Theme nonTargetTheme = themeRepository.save(Theme.create("레벨3 탈출", "우테코 레벨3를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        LocalDate targetDate = LocalDate.of(2023, 8, 5);

        saveReservation("브라운", targetDate, time, targetTheme);
        saveReservation("브라운", LocalDate.of(2024, 9, 10), time, targetTheme);
        saveReservation("브라운", targetDate, time, nonTargetTheme);

        // when
        List<ReservationTimeAvailability> availableTimes = reservationTimeService.findAvailableTimes(targetDate, targetTheme.getId());

        // then
        assertThat(availableTimes).hasSize(2)
                .extracting(ReservationTimeAvailability::getReservationTime,
                        ReservationTimeAvailability::isAvailable)
                .containsExactlyInAnyOrder(Tuple.tuple(time, false), Tuple.tuple(time2, true));
    }

    @Test
    @DisplayName("특정 날짜 및 테마의 예약 가능한 시간들을 찾을 때 테마 id가 없으면 예외가 발생한다.")
    public void findAvailableTimes_fail() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long notFoundThemeId = 37L;

        // when, then
        assertThatThrownBy(() -> reservationTimeService.findAvailableTimes(date, notFoundThemeId))
                .isInstanceOf(NotFoundException.class);
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(Reservation.create(
                name,
                date,
                time,
                theme,
                LocalDateTime.of(date, time.getStartAt()).minusMinutes(1)
        ));
    }
}
