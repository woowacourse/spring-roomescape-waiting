package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ReservationDate {
    @Column(name = "date", nullable = false)
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        this.value = Objects.requireNonNull(value);
    }
}
