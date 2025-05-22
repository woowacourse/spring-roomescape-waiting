package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    ReservationSpec spec;

    public Reservation(Member member, ReservationSpec spec) {
        this.member = Objects.requireNonNull(member);
        this.spec = Objects.requireNonNull(spec);
    }

    public Reservation(Long id, Member member, ReservationSpec spec) {
        this.id = Objects.requireNonNull(id);
        this.member = Objects.requireNonNull(member);
        this.spec = Objects.requireNonNull(spec);
    }

    public LocalDate getDate() {
        return spec.getDate().getValue();
    }

    public ReservationTime getTime() {
        return spec.getTime();
    }

    public Theme getTheme() {
        return spec.getTheme();
    }
}
