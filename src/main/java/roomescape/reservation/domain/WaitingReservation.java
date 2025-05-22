package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitingReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ReservationTime time;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theme theme;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Builder
    private WaitingReservation(
            final Long id,
            @NonNull final LocalDate date,
            @NonNull final ReservationTime time,
            @NonNull final Theme theme,
            @NonNull final Member member
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }
}
