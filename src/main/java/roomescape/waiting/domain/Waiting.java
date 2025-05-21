package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member reserver;
    @Embedded
    private ReservationDateTime reservationDatetime;
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Builder
    public Waiting(Member reserver, ReservationDateTime reservationDateTime, Theme theme) {
        this.reserver = reserver;
        this.reservationDatetime = reservationDateTime;
        this.theme = theme;
    }

    public boolean isOwner(Long memberId) {
        return reserver.getId().equals(memberId);
    }

    public LocalDate getDate() {
        return reservationDatetime.getDate();
    }

    public ReservationTime getReservationTime() {
        return reservationDatetime.getReservationTime();
    }

    public LocalTime getStartAt() {
        return reservationDatetime.getStartAt();
    }
}
