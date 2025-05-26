package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.WaitingWithRank;

public record UserReservedRecordsResponse(
        long id,
        ThemeResponse theme,
        LocalDate date,
        TimeSlotResponse time,
        String status

) {

    public static List<UserReservedRecordsResponse> fromReservations(
            final List<Reservation> reservations) {
        return reservations.stream()
                .map(UserReservedRecordsResponse::fromReservation)
                .toList();
    }

    private static UserReservedRecordsResponse fromReservation(final Reservation reservation) {
        return new UserReservedRecordsResponse(
                reservation.id(),
                ThemeResponse.from(reservation.theme()),
                reservation.date(),
                TimeSlotResponse.from(reservation.timeSlot()),
                "예약"
        );
    }

    public static List<UserReservedRecordsResponse> fromWaitingsWithRank(
            final List<WaitingWithRank> waitingsWithRanks) {
        return waitingsWithRanks.stream()
                .map(UserReservedRecordsResponse::fromWaitingWithRank)
                .toList();
    }

    private static UserReservedRecordsResponse fromWaitingWithRank(final WaitingWithRank waitingWithRank) {
        return new UserReservedRecordsResponse(
                waitingWithRank.waiting().id(),
                ThemeResponse.from(waitingWithRank.waiting().theme()),
                waitingWithRank.waiting().date(),
                TimeSlotResponse.from(waitingWithRank.waiting().timeSlot()),
                waitingWithRank.rank() + "번째 예약대기"
        );
    }
}
