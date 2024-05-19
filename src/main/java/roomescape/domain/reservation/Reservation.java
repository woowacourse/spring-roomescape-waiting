package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime time;

    public Reservation(Member member, Theme theme, LocalDate date, ReservationTime time) {
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
    }

    protected Reservation() {
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String memberName() {
        return member.getName();
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public String memberEmail() {
        return member.getEmail();
    }

    public Long themeId() {
        return theme.getId();
    }
}
