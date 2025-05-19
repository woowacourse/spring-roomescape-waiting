package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import roomescape.exception.reservation.ReservationFieldRequiredException;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(date, time, theme, member);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, date, time, theme, member);
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new ReservationFieldRequiredException("날짜");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new ReservationFieldRequiredException("시간");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ReservationFieldRequiredException("테마");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new ReservationFieldRequiredException("멤버");
        }
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
}
