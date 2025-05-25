package roomescape.domain.reservation;

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
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Embedded
    private ReservationSlot reservationSlot;

    public Reservation(Long id,
                       Member member,
                       LocalDate date,
                       ReservationTime time,
                       Theme theme,
                       ReservationStatus status) {
        this(id, member, status, new ReservationSlot(date, time, theme));
    }

    public Reservation(Long id, Member member, ReservationStatus status, ReservationSlot reservationSlot) {
        this.id = id;
        this.member = member;
        this.status = status;
        this.reservationSlot = reservationSlot;
    }

    protected Reservation() {
    }

    public static Reservation create(LocalDateTime currentDateTime, Member member, ReservationSlot reservationSlot) {
        reservationSlot.validateReservable(currentDateTime);
        return new Reservation(null, member, ReservationStatus.RESERVE, reservationSlot);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return reservationSlot.date();
    }

    public ReservationTime getTime() {
        return reservationSlot.time();
    }

    public Theme getTheme() {
        return reservationSlot.theme();
    }

    public boolean isEqualThemeId(Long themeId) {
        return reservationSlot.theme().getId().equals(themeId);
    }

    public ReservationStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Reservation that = (Reservation) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMember(), that.getMember())
                && getStatus() == that.getStatus() && Objects.equals(reservationSlot, that.reservationSlot);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getMember());
        result = 31 * result + Objects.hashCode(getStatus());
        result = 31 * result + Objects.hashCode(reservationSlot);
        return result;
    }
}
