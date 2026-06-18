package roomescape.domain.reservationdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    public ReservationDate(Long id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public static ReservationDate of(long dateId, LocalDate date) {
        return new ReservationDate(dateId, date);
    }

    public static ReservationDate createWithoutId(LocalDate reservationDate) {
        return new ReservationDate(
            null,
            reservationDate
        );
    }

    public boolean isBefore(LocalDate compareDate) {
        return date.isBefore(compareDate);
    }

    public boolean isSame(LocalDate compareDate) {
        return date.isEqual(compareDate);
    }
}
