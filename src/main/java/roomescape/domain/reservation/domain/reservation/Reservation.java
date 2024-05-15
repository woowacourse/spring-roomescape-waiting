package roomescape.domain.reservation.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.domain.Role;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date; //todo: reservationDate 검증
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    public Reservation() {

    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
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

    public String getMemberName() {
        return member.getName();
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

    public String getMemberEmail() {
        return member.getEmail();
    }

    public Role getMemberRole() {
        return member.getRole();
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
