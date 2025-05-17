package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class BookedStatus {

    private int sequence;

    public static BookedStatus from(final int sequence) {
        return new BookedStatus(sequence);
    }

    public static BookedStatus from(final boolean value) {
        if (value) {
            return new BookedStatus(0);
        }
        final int unBookedSequence = 1; //Todo. 3단계 수정 예정
        return new BookedStatus(unBookedSequence);
    }

    public boolean isBooked() {
        return sequence == 0;
    }
}
