package roomescape.reservation.domain.waiting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.timeslot.TimeSlot;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
@Table(name = "RESERVATION_WAITING")
public class Waiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private TimeSlot time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Waiting(final LocalDate date, final TimeSlot time, final Theme theme, Member member) {
        this(null, date, time, theme, member);
    }
}
