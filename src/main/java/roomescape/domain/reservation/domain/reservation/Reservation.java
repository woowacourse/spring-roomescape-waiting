package roomescape.domain.reservation.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Reservation() {

    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.member = member;
        this.date = new ReservationDate(date);
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme)
                && Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, theme, member);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                '}';
    }
}
