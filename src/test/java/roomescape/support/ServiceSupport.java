package roomescape.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.order.repository.OrderRepository;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.repository.JdbcReservationSlotRepository;
import roomescape.slot.repository.ReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDateTime;

@JdbcTest
@Import({
        JdbcReservationRepository.class,
        JdbcReservationSlotRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationDateRepository.class,
        JdbcThemeRepository.class,
        OrderRepository.class,
        PaymentRepository.class
})
public abstract class ServiceSupport {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationSlotRepository reservationSlotRepository;

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

    protected Theme saveTheme(Theme theme) {
        return themeRepository.save(theme);
    }

    protected Reservation saveReservation(String name, ReservationSlot slot) {
        return reservationRepository.save(ReservationFixture.reservation(name, slot));
    }

    protected Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    protected Reservation savePastReservation(String name, ReservationSlot slot) {
        return reservationRepository.save(Reservation.load(0L, name, slot.getId(), ReservationStatus.RESERVED, LocalDateTime.now()));
    }

    protected Reservation saveWaitReservation(String name, ReservationSlot slot) {
        return reservationRepository.save(ReservationFixture.waitReservation(name, slot));
    }

    protected ReservationSlot saveSlot(ReservationSlot slot) {
        return reservationSlotRepository.save(slot);
    }

}
