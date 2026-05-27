package roomescape.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;

@JdbcTest
@Import({
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationDateRepository.class,
        JdbcThemeRepository.class
})
public abstract class ServiceSupport {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository reservationTimeRepository;

    @Autowired
    protected ReservationDateRepository reservationDateRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    protected ReservationDate saveDate(ReservationDate reservationDate) {
        return reservationDateRepository.save(reservationDate);
    }

    protected ReservationTime saveTime(ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

    protected Theme saveTheme(String themeName) {
        return themeRepository.save(ThemeFixture.activeTheme(themeName));
    }

    protected Reservation saveReservation(String name, ReservationDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(ReservationFixture.reservation(name, date, time, theme));
    }

    protected Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    protected Reservation saveWaitReservation(String name, ReservationDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(ReservationFixture.waitReservation(name, date, time, theme));
    }

}
