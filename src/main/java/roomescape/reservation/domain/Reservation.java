package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Member member;

    private Reservation(final Long id,
                        final LocalDate date,
                        final ReservationTime time,
                        final Theme theme,
                        final Member member
    ) {
        validateIsNonNull(date);
        validateIsNonNull(time);
        validateIsNonNull(theme);
        validateIsNonNull(member);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("예약 정보는 null 일 수 없습니다.");
        }
    }

    public static Reservation createWithoutId(final LocalDate date,
                                              final ReservationTime time,
                                              final Theme theme,
                                              final Member member
    ) {
        return new Reservation(null, date, time, theme, member);
    }

    public static Reservation createWithId(final Long id,
                                           final LocalDate date,
                                           final ReservationTime time,
                                           final Theme theme,
                                           final Member member
    ) {
        return new Reservation(Objects.requireNonNull(id), date, time, theme, member);
    }

    public Reservation assignId(final Long id) {
        return new Reservation(Objects.requireNonNull(id), date, time, theme, member);
    }

    public boolean isCanReserveDateTime(final LocalDateTime dateTime) {
        if (date.isBefore(dateTime.toLocalDate())) {
            return true;
        }

        return date.isEqual(dateTime.toLocalDate()) && time.isBefore(dateTime.toLocalTime());
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.isEqual(time.getStartAt());
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
    public boolean equals(final Object object) {
        if (!(object instanceof Reservation that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
