package roomescape.date.fixture;

import roomescape.date.domain.ReservationDate;

import java.time.LocalDate;

public class ReservationDateFixture {

    public static ReservationDate oneWeekLater() {
        return ReservationDate.create(LocalDate.now().plusWeeks(1));
    }

    public static ReservationDate twoWeeksLater() {
        return ReservationDate.create(LocalDate.now().plusWeeks(2));
    }

    public static ReservationDate activeOneWeekLater() {
        ReservationDate reservationDate = ReservationDate.create(LocalDate.now().plusWeeks(1));
        reservationDate.updateStatus(true);
        return reservationDate;
    }

    public static ReservationDate activeTwoWeekLater() {
        ReservationDate reservationDate = ReservationDate.create(LocalDate.now().plusWeeks(2));
        reservationDate.updateStatus(true);
        return reservationDate;
    }

    public static ReservationDate inActiveTwoWeekLater() {
        ReservationDate reservationDate = ReservationDate.create(LocalDate.now().plusWeeks(2));
        reservationDate.updateStatus(false);
        return reservationDate;
    }

    public static ReservationDate pastDate() {
        return ReservationDate.load(0L, LocalDate.now().minusDays(1), true);
    }

}
