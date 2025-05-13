package roomescape.domain.reservation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Member;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public void validateReservable(LocalDateTime currentDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.startAt());
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new BusinessRuleViolationException("지난 날짜와 시간에 대한 예약은 불가능합니다.");
        }
        Duration duration = Duration.between(currentDateTime, reservationDateTime);
        if (duration.toMinutes() < 10) {
            throw new BusinessRuleViolationException("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
        }
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public boolean isEqualThemeId(Long themeId) {
        return theme.getId().equals(themeId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id)
                && Objects.equals(member, that.member)
                && Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
    }
}
