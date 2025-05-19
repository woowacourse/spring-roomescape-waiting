package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class BookedCount {

    private final long value;

    public static BookedCount from(final long value) {
        validateNegative(value);
        return new BookedCount(value);
    }

    private static void validateNegative(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("BookedCount must not be negative: " + value);
        }
    }
}
