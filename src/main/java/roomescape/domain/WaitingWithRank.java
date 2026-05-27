package roomescape.domain;

import java.time.LocalDate;

public record WaitingWithRank(
        Long id, String name, LocalDate reservationDate, ReservationTime reservationTime, Theme reservationTheme, int rank) {
}
