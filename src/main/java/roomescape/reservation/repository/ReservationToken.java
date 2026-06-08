package roomescape.reservation.repository;

import roomescape.reservation.domain.Status;

import java.util.UUID;


public record ReservationToken(
        String confirmToken,
        String waitingToken
) {
    private static final String LOCK_TOKEN = "0";
    public static ReservationToken from(Status status) {
        return switch (status) {
            case CONFIRMED -> new ReservationToken(
                    LOCK_TOKEN,
                    UUID.randomUUID().toString()
            );
            case WAITING -> new ReservationToken(
                    UUID.randomUUID().toString(),
                    LOCK_TOKEN
            );
            case CANCELED -> new ReservationToken(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );
        };
    }
}
