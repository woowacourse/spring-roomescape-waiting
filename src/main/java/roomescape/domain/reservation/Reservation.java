package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        this(null, member, date, reservationTime, theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
    }
}
