package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

import static roomescape.domain.Reservation.Status.RESERVED;

@Getter
@EqualsAndHashCode(of = "id")
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    protected Reservation() {
    }

    @Builder
    public Reservation(Member member, LocalDate date, ReservationTime reservationTime, Theme theme, Status reservationStatus) {
        this(null, member, date, reservationTime, theme, reservationStatus);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime reservationTime, Theme theme, Status status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isReserved() {
        return this.status == RESERVED;
    }

    public boolean isWaiting() {
        return this.status == Status.WAITING;
    }

    public enum Status {
        RESERVED,
        WAITING,
        ;
    }
}
