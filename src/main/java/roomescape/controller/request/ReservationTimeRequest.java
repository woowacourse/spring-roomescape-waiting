package roomescape.controller.request;

import java.time.LocalTime;

import jakarta.annotation.Nonnull;

public record ReservationTimeRequest(
        @Nonnull
        LocalTime startAt) {
}
