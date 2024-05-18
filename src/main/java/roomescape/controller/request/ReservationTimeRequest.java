package roomescape.controller.request;

import jakarta.annotation.Nonnull;

import java.time.LocalTime;

public record ReservationTimeRequest(
        @Nonnull
        LocalTime startAt) {
}
