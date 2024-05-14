package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final Member member;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(LocalDate date, Member member, ReservationTime time, Theme theme) {
        this(null, date, member, time, theme);
    }

    public Reservation(Long id, LocalDate date, Member member, ReservationTime time, Theme theme) {
        validate(date, member, time, theme);

        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    private void validate(LocalDate date, Member member, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수 값입니다.");
        }

        if (member == null) {
            throw new IllegalArgumentException("회원은 필수 값입니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 필수 값입니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수 값입니다.");
        }
    }


    public boolean isBefore(LocalDateTime dateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        return reservationDateTime.isBefore(dateTime);
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }
}
