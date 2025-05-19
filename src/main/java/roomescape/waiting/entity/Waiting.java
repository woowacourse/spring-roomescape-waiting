package roomescape.waiting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Member member;

    public Waiting(LocalDate date, Theme theme, ReservationTime time, Member member) {
        this(null, date, theme, time, member);
    }
}
