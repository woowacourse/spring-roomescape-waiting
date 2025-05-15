package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import roomescape.domain.BusinessRuleViolationException;

public class DailyThemeReservations {

    private final List<Reservation> reservations;

    public DailyThemeReservations(List<Reservation> reservations, Long themeId, LocalDate reservationDate) {
        validate(reservations, themeId, reservationDate);
        this.reservations = reservations;
    }

    private void validate(List<Reservation> reservations, Long themeId, LocalDate reservationDate) {
        for (Reservation reservation : reservations) {
            if (!reservation.isEqualThemeId(themeId) || !reservation.getDate().equals(reservationDate)) {
                throw new BusinessRuleViolationException("특정 테마, 특정 날짜에 속한 예약이 아닙니다.");
            }
        }
    }

    public Set<ReservationTime> calculateBookedTimes() {
        return reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
    }
}
