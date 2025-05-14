package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public static Reservation create(
            Member member,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new Reservation(null, member, date, time, theme);
    }

    public static Reservation create(
            Long id,
            Member member,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        return new Reservation(id, member, date, time, theme);
    }
}
