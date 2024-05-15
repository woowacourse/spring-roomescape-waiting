package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    public Reservation(Member member, Theme theme, LocalDate date, ReservationTime time) {
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
    }

    public Reservation(Long memberId, String email, String password, String memberName, String role,
                       Long themeId, String themeName, String description, String thumbnail, String date, Long timeId,
                       String time) {
        this(
                new Member(memberId, email, password, memberName, role),
                new Theme(themeId, themeName, description, thumbnail),
                LocalDate.parse(date),
                new ReservationTime(timeId, time)
        );
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

    public Long memberId() {
        return member.getId();
    }

    public String themeName() {
        return theme.getName();
    }

    public Long themeId() {
        return theme.getId();
    }

    public Long timeId() {
        return time.getId();
    }
}
