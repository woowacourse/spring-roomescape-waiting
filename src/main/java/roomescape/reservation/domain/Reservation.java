package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.BatchUpdateException;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member reserver;
    @Embedded
    private ReservationDateTime reservationDateTime;
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Builder
    public Reservation(Member reserver, ReservationDateTime reservationDateTime, Theme theme,
                       ReservationStatus status) {
        this.reserver = reserver;
        this.reservationDateTime = reservationDateTime;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation reserve(
            final Member reserver,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    ) {
        return Reservation.builder()
                .reserver(reserver)
                .reservationDateTime(reservationDateTime)
                .theme(theme)
                .status(ReservationStatus.RESERVED)
                .build();
    }

    public static Reservation from(Waiting waiting) {
        return Reservation.builder()
                .reserver(waiting.getReserver())
                .reservationDateTime(waiting.getReservationDateTime())
                .theme(waiting.getTheme())
                .build();
    }

    public boolean isOwner(Long userId) {
        return reserver.getId().equals(userId);
    }

    public String getReserverName() {
        return reserver.getName();
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
