package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import roomescape.reservation.exception.InvalidStatusTransitionException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member reserver;
    @Embedded
    private ReservationDateTime reservationDateTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Builder
    private Reservation(Member reserver, ReservationDateTime reservationDateTime, Theme theme,
                       ReservationStatus status) {
        this.reserver = reserver;
        this.reservationDateTime = reservationDateTime;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation reserve(final Member reserver, final ReservationDateTime reservationDateTime,
                                      final Theme theme
    ) {
        return Reservation.builder()
                .reserver(reserver)
                .reservationDateTime(reservationDateTime)
                .theme(theme)
                .status(ReservationStatus.RESERVED)
                .build();
    }

    public static Reservation waiting(final Member reserver, final ReservationDateTime reservationDateTime,
                                      final Theme theme
    ) {
        return Reservation.builder()
                .reserver(reserver)
                .reservationDateTime(reservationDateTime)
                .theme(theme)
                .status(ReservationStatus.WAITING)
                .build();
    }

    public boolean isOwner(Long userId) {
        return reserver.getId().equals(userId);
    }

    public void reserved() {
        if (status != ReservationStatus.WAITING) {
            throw new InvalidStatusTransitionException("예약할 수 없는 상태입니다.");
        }

        status = ReservationStatus.RESERVED;
    }

    public void cancelReservation() {
        if (status != ReservationStatus.RESERVED) {
            throw new InvalidStatusTransitionException("예약이 되어 있지 않습니다.");
        }

        status = ReservationStatus.CANCELED_RESERVATION;
    }

    public void cancelWaiting() {
        if (status != ReservationStatus.WAITING) {
            throw new InvalidStatusTransitionException("대기 예약이 되어 있지 않습니다.");
        }

        status = ReservationStatus.CANCELED_WAITING;
    }

    public String getReserverName() {
        return reserver.getName();
    }

    public String getThemeName() {
        return theme.getName();
    }

    public LocalDate getDate() {
        return reservationDateTime.getDate();
    }

    public LocalTime getStartAt() {
        return reservationDateTime.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationDateTime.getReservationTime();
    }

    public Long getTimeId() {
        return reservationDateTime.getTimeId();
    }
}
