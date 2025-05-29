package roomescape.waiting.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member waiter;

    @Embedded
    private ReservationDateTime reservationDatetime;

    @JoinColumn(name = "theme_id")
    @ManyToOne
    private Theme theme;

    private LocalDateTime waitedAt;

    private Waiting(
            Long id,
            Member waiter,
            ReservationDateTime reservationDatetime,
            Theme theme,
            LocalDateTime waitedAt
    ) {
        this.id = id;
        this.waiter = waiter;
        this.reservationDatetime = reservationDatetime;
        this.theme = theme;
        this.waitedAt = waitedAt;
    }

    public static Waiting wait(
            Member waiter,
            ReservationDateTime reservationDatetime,
            Theme theme,
            LocalDateTime waitedAt
    ) {
        return new Waiting(null, waiter, reservationDatetime, theme, waitedAt);
    }
}
