package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class BookedStatus {

    private final boolean value;

    public static BookedStatus from(final boolean value) {
        return new BookedStatus(value);
    }

    public boolean isBooked() {
        return value;
    }
}
