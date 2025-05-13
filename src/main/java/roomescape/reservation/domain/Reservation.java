package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public final class Reservation {

    private final Long id;
    private final Member member;
    private final ReservationDateTime dateTime;
    private final Theme theme;

    public Reservation(final Long id, final Member member, final ReservationDateTime dateTime, final Theme theme) {
        validateNotNull(member, dateTime, theme);
        this.id = id;
        this.member = member;
        this.dateTime = dateTime;
        this.theme = theme;
    }

    public static Reservation register(final Member member, final LocalDate date,
                                       final ReservationTime time, final Theme theme) {
        final ReservationDateTime dateTime = new ReservationDateTime(date, time);
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("지나간 날짜와 시간은 예약 불가합니다.");
        }
        return new Reservation(null, member, dateTime, theme);
    }

    public Reservation withId(final long id) {
        return new Reservation(id, member, dateTime, theme);
    }

    private void validateNotNull(final Member member, final ReservationDateTime dateTime, final Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("사용자를 입력해야 합니다.");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("날짜와 시간을 입력해야 합니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDateTime getDateTime() {
        return dateTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return dateTime.getTime();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
