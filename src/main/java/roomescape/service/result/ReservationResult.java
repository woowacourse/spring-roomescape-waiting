package roomescape.service.result;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;

public record ReservationResult(
        long reservationId,
        LocalDate date,
        ThemeRegisterResult theme,
        ReservationTimeResult time,
        ReservationEntryResult entry
) {

    public static ReservationResult from(Reservation reservation) {
        ReservationEntry entry = reservation.getEntries()
                .stream()
                .filter(ReservationEntry::isReserved)
                .findFirst()
                .orElse(null);

        return from(reservation, entry);
    }

    public static ReservationResult from(Reservation reservation, long entryId) {
        ReservationEntry entry = reservation.getEntries()
                .stream()
                .filter(reservationEntry -> reservationEntry.isSameId(entryId))
                .findFirst()
                .orElse(null);

        return from(reservation, entry);
    }

    private static ReservationResult from(Reservation reservation, ReservationEntry entry) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getDate(),
                ThemeRegisterResult.from(reservation.getTheme()),
                ReservationTimeResult.from(reservation.getTime()),
                entry == null ? null : ReservationEntryResult.from(entry)
        );
    }
}
