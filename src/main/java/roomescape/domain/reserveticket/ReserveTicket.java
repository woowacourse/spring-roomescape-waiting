package roomescape.domain.reserveticket;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

@Entity
public class ReserveTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public ReserveTicket() {

    }

    public ReserveTicket(Long id, Reservation reservation, Member member) {
        this.id = id;
        this.reservation = reservation;
        this.member = member;
    }

    public Long getReservationId() {
        return reservation.getId();
    }

    public String getThemeName() {
        return reservation.getThemeName();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public LocalTime getStartAt() {
        return reservation.getStartAt();
    }

    public String getName() {
        return member.getName();
    }

    public String getStatus() {
        return reservation.getReservationStatus();
    }

    public long getId() {
        return id;
    }

    public long getMemberId() {
        return member.getId();
    }
}
