package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;

public final class Reservation {

    private final Long id;
    private final Member member;
    private final ReservationDateTime dateTime;
    private final Theme theme;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        validateMember(member);
        validateTheme(theme);
        this.id = id;
        this.member = member;
        this.dateTime = new ReservationDateTime(date, time);
        this.theme = theme;
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("사용자를 입력해야 합니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
    }

    public boolean isBefore(final LocalDateTime other) {
        return dateTime.isBefore(other);
    }

    public boolean isSameTime(final ReservationTime reservationTime) {
        return dateTime.isSameTime(reservationTime);
    }

    public boolean isMemberHasSameId(final long other) {
        return member.hasSameId(other);
    }

    public boolean isThemeHasSameId(final long other) {
        return theme.hasSameId(other);
    }

    public boolean isBetween(final LocalDate from, final LocalDate to) {
        return dateTime.isBetween(from, to);
    }

    public Member getMember() {
        return member;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return dateTime.getDate();
    }

    public ReservationTime getTime() {
        return dateTime.getTime();
    }

    public Theme getTheme() {
        return this.theme;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
