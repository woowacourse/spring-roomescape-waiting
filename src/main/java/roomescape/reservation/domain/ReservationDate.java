package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class ReservationDate {
    @Column(name = "date", nullable = false)
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        this.value = value;
    }

    public boolean isInPast() {
        return value.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return value.isEqual(LocalDate.now());
    }
}
