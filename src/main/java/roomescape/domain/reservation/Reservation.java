package roomescape.domain.reservation;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Reservation() {
    }

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public Reservation(Long id, LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getMemberId() {
        return member.getId();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
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
