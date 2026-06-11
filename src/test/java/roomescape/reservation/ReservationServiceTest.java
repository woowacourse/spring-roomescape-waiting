package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;
import roomescape.support.FakeReservationRepository;
import roomescape.support.FakeReservationSlotRepository;
import roomescape.support.FakeReservationTimeRepository;
import roomescape.support.FakeReservationWaitingRepository;
import roomescape.support.FakeThemeRepository;

class ReservationServiceTest {

    @Test
    @DisplayName("예약을 저장한다")
    void save() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        ReservationTime time = fixture.saveTime("10:00");
        LocalDate date = LocalDate.parse("2026-08-06");
        fixture.saveSlot(date, theme, time);

        Reservation saved = fixture.reservationService.save("쿠다", date, theme.getId(), time.getId());

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(fixture.reservationRepository.findById(saved.getId())).contains(saved);
    }

    @Test
    @DisplayName("같은 날짜, 테마, 시간 조합의 중복 예약은 저장할 수 없다")
    void saveDuplicateReservation() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        ReservationTime time = fixture.saveTime("10:00");
        LocalDate date = LocalDate.parse("2026-08-06");
        fixture.saveSlot(date, theme, time);
        fixture.reservationService.save("쿠다", date, theme.getId(), time.getId());

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.save("아루", date, theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("과거 날짜로는 예약할 수 없다")
    void savePastDate() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        ReservationTime time = fixture.saveTime("10:00");
        LocalDate pastDate = LocalDate.now().minusDays(1);
        fixture.saveSlot(pastDate, theme, time);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", pastDate, theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("오늘 날짜라도 이미 지난 시간으로는 예약할 수 없다")
    void savePastTimeToday() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.saveTheme();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = fixture.reservationTimeRepository.save(ReservationTime.createNew(pastTime));
        fixture.saveSlot(LocalDate.now(), theme, time);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", LocalDate.now(), theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("예약을 삭제한다")
    void deleteById() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.saveReservation("쿠다", "2026-08-06", "10:00");

        fixture.reservationService.deleteById(reservation.getId());

        assertThat(fixture.reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약은 삭제할 수 없다")
    void deleteByIdNotFound() {
        Fixture fixture = new Fixture();

        assertThrows(ResourceNotFoundException.class, () -> fixture.reservationService.deleteById(1L));
    }

    @Test
    @DisplayName("예약을 삭제하면 첫 번째 대기가 예약으로 전환된다")
    void deleteByIdWithWaiting() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.saveReservation("쿠다", "2026-08-06", "10:00");
        ReservationWaiting firstWaiting = fixture.reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation,
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));
        ReservationWaiting secondWaiting = fixture.reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation,
                "도기",
                LocalDateTime.parse("2026-08-05T12:01:00")
        ));

        fixture.reservationService.deleteById(reservation.getId());

        assertThat(fixture.reservationRepository.findById(reservation.getId())).isEmpty();
        assertThat(fixture.reservationRepository.findAll())
                .extracting(Reservation::getName)
                .containsExactly("아루");
        assertThat(fixture.reservationWaitingRepository.findById(firstWaiting.getId())).isEmpty();
        assertThat(fixture.reservationWaitingRepository.findById(secondWaiting.getId())).contains(secondWaiting);
    }

    @Test
    @DisplayName("지난 예약은 취소할 수 없다")
    void deleteByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.savePastReservation();

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.deleteByIdAndName(reservation.getId(), "쿠다")
        );
    }

    @Test
    @DisplayName("예약자 이름이 일치하지 않으면 예약을 취소할 수 없다")
    void deleteByIdAndNameMismatch() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.saveReservation("쿠다", "2026-08-06", "10:00");

        assertThrows(
                ResourceNotFoundException.class,
                () -> fixture.reservationService.deleteByIdAndName(reservation.getId(), "아루")
        );
    }

    @Test
    @DisplayName("조회한 이름의 예약 날짜와 시간을 변경한다")
    void updateByIdAndName() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.saveReservation("쿠다", "2026-08-06", "10:00");
        ReservationTime secondTime = fixture.saveTime("11:00");
        fixture.saveSlot(LocalDate.parse("2026-08-07"), reservation.getTheme(), secondTime);

        Reservation updated = fixture.reservationService.updateByIdAndName(
                reservation.getId(),
                "쿠다",
                LocalDate.parse("2026-08-07"),
                secondTime.getId()
        );

        assertThat(updated.getDate()).isEqualTo(LocalDate.parse("2026-08-07"));
        assertThat(updated.getTime()).isEqualTo(secondTime);
        assertThat(fixture.reservationRepository.findById(reservation.getId())).contains(updated);
    }

    @Test
    @DisplayName("조회한 이름의 다른 예약과 시간이 겹치면 변경할 수 없다")
    void updateByIdAndNameDuplicate() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.saveReservation("쿠다", "2026-08-06", "10:00");
        ReservationTime secondTime = fixture.saveTime("11:00");
        fixture.saveSlot(LocalDate.parse("2026-08-06"), reservation.getTheme(), secondTime);
        fixture.reservationService.save("아루", LocalDate.parse("2026-08-06"), reservation.getTheme().getId(), secondTime.getId());

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        reservation.getId(),
                        "쿠다",
                        LocalDate.parse("2026-08-06"),
                        secondTime.getId()
                )
        );
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void updateByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Reservation reservation = fixture.savePastReservation();
        ReservationTime secondTime = fixture.saveTime("11:00");

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        reservation.getId(),
                        "쿠다",
                        LocalDate.now().plusDays(1),
                        secondTime.getId()
                )
        );
    }

    private static class Fixture {
        private final FakeThemeRepository themeRepository = new FakeThemeRepository();
        private final FakeReservationRepository reservationRepository = new FakeReservationRepository();
        private final FakeReservationSlotRepository reservationSlotRepository = new FakeReservationSlotRepository();
        private final FakeReservationWaitingRepository reservationWaitingRepository = new FakeReservationWaitingRepository();
        private final FakeReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
        private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository);
        private final ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                reservationSlotRepository,
                themeService
        );
        private final ReservationService reservationService = new ReservationService(
                reservationRepository,
                reservationSlotRepository,
                reservationWaitingRepository,
                reservationTimeService,
                themeService
        );

        private Theme saveTheme() {
            return themeRepository.save(
                    Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
            );
        }

        private ReservationTime saveTime(final String startAt) {
            return reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse(startAt)));
        }

        private Reservation saveReservation(final String name, final String date, final String startAt) {
            Theme theme = themeRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseGet(this::saveTheme);
            ReservationTime time = saveTime(startAt);
            saveSlot(LocalDate.parse(date), theme, time);

            return reservationService.save(name, LocalDate.parse(date), theme.getId(), time.getId());
        }

        private ReservationSlot saveSlot(final LocalDate date, final Theme theme, final ReservationTime time) {
            return reservationSlotRepository.save(new ReservationSlot(date, theme, time));
        }

        private Reservation savePastReservation() {
            Theme theme = saveTheme();
            ReservationTime time = saveTime("10:00");
            ReservationSlot slot = saveSlot(LocalDate.now().minusDays(1), theme, time);
            return reservationRepository.save(new Reservation(
                    1L,
                    "쿠다",
                    slot,
                    LocalDateTime.now().minusDays(2)
            ));
        }
    }
}
