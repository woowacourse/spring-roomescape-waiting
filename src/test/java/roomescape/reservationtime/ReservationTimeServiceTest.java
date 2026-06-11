package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;
import roomescape.support.FakeReservationRepository;
import roomescape.support.FakeReservationSlotRepository;
import roomescape.support.FakeReservationTimeRepository;
import roomescape.support.FakeThemeRepository;

class ReservationTimeServiceTest {

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save() {
        Fixture fixture = new Fixture();

        ReservationTime saved = fixture.reservationTimeService.save(LocalTime.parse("10:00"));

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(fixture.reservationTimeRepository.findById(saved.getId())).contains(saved);
    }

    @Test
    @DisplayName("중복된 예약 시간은 저장할 수 없다")
    void saveDuplicateTime() {
        Fixture fixture = new Fixture();
        fixture.reservationTimeService.save(LocalTime.parse("10:00"));

        assertThrows(ConflictException.class, () -> fixture.reservationTimeService.save(LocalTime.parse("10:00")));
    }

    @Test
    @DisplayName("특정 날짜와 테마에 이미 예약된 시간을 제외한 예약 가능 시간을 조회한다")
    void findAvailableTimes() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        ReservationTime ten = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime eleven = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        LocalDate date = LocalDate.parse("2026-08-06");
        ReservationSlot reservedSlot = fixture.saveSlot(date, theme, ten);
        fixture.saveSlot(date, theme, eleven);
        fixture.reservationRepository.save(new Reservation(
                "쿠다",
                reservedSlot,
                LocalDate.now().atStartOfDay()
        ));

        List<ReservationTime> availableTimes = fixture.reservationTimeService.findAvailableTimes(date, theme.getId());

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .containsExactly(eleven.getId());
    }

    @Test
    @DisplayName("지난 날짜에 대해서는 예약 가능 시간이 조회되지 않는다")
    void findAvailableTimesInPastDate() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime ten = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime eleven = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        fixture.saveSlot(pastDate, theme, ten);
        fixture.saveSlot(pastDate, theme, eleven);

        List<ReservationTime> availableTimes = fixture.reservationTimeService.findAvailableTimes(pastDate, theme.getId());

        assertThat(availableTimes).isEmpty();
    }

    @Test
    @DisplayName("오늘 날짜 조회 시 이미 지난 시간은 예약 가능 시간에서 제외된다")
    void findAvailableTimesTodayExcludesPastTimes() {
        assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 59)));

        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        LocalTime futureTime = now.plusMinutes(1);
        ReservationTime past = fixture.reservationTimeRepository.save(ReservationTime.createNew(pastTime));
        ReservationTime future = fixture.reservationTimeRepository.save(ReservationTime.createNew(futureTime));
        fixture.saveSlot(LocalDate.now(), theme, past);
        fixture.saveSlot(LocalDate.now(), theme, future);

        List<ReservationTime> availableTimes = fixture.reservationTimeService.findAvailableTimes(LocalDate.now(), theme.getId());

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .contains(future.getId())
                .doesNotContain(past.getId());
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간은 삭제할 수 없다")
    void deleteById() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        ReservationTime time = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationSlot slot = new ReservationSlot(LocalDate.parse("2026-08-06"), theme, time);
        fixture.reservationRepository.save(new Reservation("쿠다", slot, LocalDate.now().atStartOfDay()));

        assertThrows(ConflictException.class, () -> fixture.reservationTimeService.deleteById(time.getId()));
    }

    @Test
    @DisplayName("예약이 없는 예약 시간을 삭제한다")
    void deleteByIdWithoutReservation() {
        Fixture fixture = new Fixture();
        ReservationTime time = fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));

        fixture.reservationTimeService.deleteById(time.getId());

        assertThat(fixture.reservationTimeRepository.findById(time.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간은 삭제할 수 없다")
    void deleteByIdNotFound() {
        Fixture fixture = new Fixture();

        assertThrows(ResourceNotFoundException.class, () -> fixture.reservationTimeService.deleteById(1L));
    }

    @Test
    @DisplayName("ID로 예약 시간을 조회한다")
    void getById() {
        Fixture fixture = new Fixture();
        ReservationTime reservationTime = fixture.reservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );

        ReservationTime found = fixture.reservationTimeService.getById(reservationTime.getId());

        assertThat(found).isEqualTo(reservationTime);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 예약 시간을 조회할 수 없다")
    void getByIdNotFound() {
        Fixture fixture = new Fixture();

        assertThrows(ResourceNotFoundException.class, () -> fixture.reservationTimeService.getById(1L));
    }

    private static class Fixture {
        private final FakeThemeRepository themeRepository = new FakeThemeRepository();
        private final FakeReservationRepository reservationRepository = new FakeReservationRepository();
        private final FakeReservationSlotRepository reservationSlotRepository = new FakeReservationSlotRepository();
        private final FakeReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
        private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository);
        private final ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                reservationSlotRepository,
                themeService
        );

        private Theme saveTheme() {
            return themeRepository.save(
                    Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
            );
        }

        private ReservationSlot saveSlot(final LocalDate date, final Theme theme, final ReservationTime time) {
            return reservationSlotRepository.save(new ReservationSlot(date, theme, time));
        }
    }
}
