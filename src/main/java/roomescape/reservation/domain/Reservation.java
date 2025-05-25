package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member reserver;

    @Embedded
    private ReservationDateTime reservationDatetime;

    @JoinColumn(name = "theme_id")
    @ManyToOne
    private Theme theme;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    private Reservation(
            Long id,
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme,
            ReservationStatus status,
            LocalDateTime reservedAt
    ) {
        this.id = id;
        this.reserver = reserver;
        this.reservationDatetime = reservationDateTime;
        this.theme = theme;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    public static Reservation reserve(
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme,
            LocalDateTime reservedAt
    ) {
        return new Reservation(
                null,
                reserver,
                reservationDateTime,
                theme,
                ReservationStatus.RESERVED,
                reservedAt
        );
    }

    public void reserve() {
        if (isWaiting()) {
            this.status = ReservationStatus.RESERVED;
        }
    }

    public static Reservation wait(
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme,
            LocalDateTime reservedAt
    ) {
        return new Reservation(
                null,
                reserver,
                reservationDateTime,
                theme,
                ReservationStatus.WAITING,
                reservedAt
        );
    }

    public boolean isReserved() {
        return status == ReservationStatus.RESERVED;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public String getReserverName() {
        return reserver.getName();
    }

    public LocalDate getDate() {
        return reservationDatetime.reservationDate().date();
    }

    public LocalTime getStartAt() {
        return reservationDatetime.reservationTime().getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationDatetime.reservationTime();
    }

    public Long getTimeId() {
        return reservationDatetime.reservationTime().getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public String getThemeName() {
        return theme.getName();
    }
}
