package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.exceptions.MissingRequiredFieldException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        validateNotNull(date, reservationTime, theme, member);
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Waiting(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this(null, date, reservationTime, theme, member);
    }

    private void validateNotNull(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        if (date == null) {
            throw new MissingRequiredFieldException(LocalDate.class.getSimpleName() + "값이 null 입니다.");
        }
        if (reservationTime == null) {
            throw new MissingRequiredFieldException(ReservationTime.class.getSimpleName() + "값이 null 입니다.");
        }
        if (theme == null) {
            throw new MissingRequiredFieldException(Theme.class.getSimpleName() + "값이 null 입니다.");
        }
        if (member == null) {
            throw new MissingRequiredFieldException(Member.class.getSimpleName() + "값이 null 입니다.");
        }
    }

    public boolean isBeforeNow() {
        return reservationTime.isBeforeNow(date);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDate(DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
