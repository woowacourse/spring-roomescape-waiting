package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.JpaReservationRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationdate.JpaReservationDateRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.JpaReservationTimeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.JpaThemeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.JpaUserRepository;
import roomescape.domain.user.User;

@Component
public class TestFixture {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationSlotRepository reservationSlotRepository;
    private final JpaUserRepository userRepository;
    private final JpaReservationDateRepository reservationDateRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public TestFixture(
        JpaReservationRepository reservationRepository,
        JpaReservationSlotRepository reservationSlotRepository,
        JpaUserRepository userRepository,
        JpaReservationDateRepository reservationDateRepository,
        JpaReservationTimeRepository reservationTimeRepository,
        JpaThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.userRepository = userRepository;
        this.reservationDateRepository = reservationDateRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public void clear() {
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        reservationDateRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
    }

    public Theme saveTheme(String name) {
        return saveTheme(name, "무서운 테마", "theme-url");
    }

    public Theme saveTheme(String name, String content, String url) {
        return themeRepository.save(Theme.createWithoutId(name, content, url));
    }

    public ReservationDate saveDate(String date) {
        return reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.parse(date)));
    }

    public ReservationTime saveTime(String time) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.parse(time)));
    }

    public ReservationSlot saveSlot(Long dateId, Long timeId, Long themeId) {
        ReservationDate date = reservationDateRepository.findById(dateId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        return saveSlot(date, time, theme);
    }

    public ReservationSlot saveSlot(ReservationDate date, ReservationTime time, Theme theme) {
        return reservationSlotRepository.save(ReservationSlot.createWithoutId(date, time, theme));
    }

    public User saveUser(String name) {
        return userRepository.findByName(name)
            .orElseGet(() -> userRepository.save(User.createWithoutId(name)));
    }

    public Reservation saveReservation(String name, ReservationSlot reservationSlot, ReservationStatus status) {
        return saveReservation(name, reservationSlot, status, DEFAULT_TIME);
    }

    public Reservation saveReservation(
        String name,
        ReservationSlot reservationSlot,
        ReservationStatus status,
        LocalDateTime dateTime
    ) {
        User user = saveUser(name);
        return reservationRepository.save(
            Reservation.createWithoutId(reservationSlot, user, status, fixedClockAt(dateTime))
        );
    }

    public Reservation saveReservation(String name, Long reservationSlotId, ReservationStatus status) {
        ReservationSlot reservationSlot = reservationSlotRepository.findById(reservationSlotId).orElseThrow();
        return saveReservation(name, reservationSlot, status);
    }

    public Reservation saveReservation(String name, String date, String time, String themeName) {
        Theme theme = saveTheme(themeName);
        ReservationDate reservationDate = saveDate(date);
        ReservationTime reservationTime = saveTime(time);
        ReservationSlot reservationSlot = saveSlot(reservationDate, reservationTime, theme);
        return saveReservation(name, reservationSlot, ReservationStatus.CONFIRMED);
    }

    public ReservationStatus findReservationStatus(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow()
            .getStatus();
    }

    private Clock fixedClockAt(LocalDateTime dateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        return Clock.fixed(dateTime.atZone(zoneId).toInstant(), zoneId);
    }
}
