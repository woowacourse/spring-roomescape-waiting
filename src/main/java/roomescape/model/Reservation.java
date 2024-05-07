package roomescape.model;

import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {

    private long id;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private Member member;

    public Reservation(long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(id, date, time, theme, member);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(date, time, theme, member);
        this.id = 0;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private Reservation() {
    }

    public static Reservation of(ReservationDto reservationDto, ReservationTime time, Theme theme, Member member) {
        return new Reservation(reservationDto.getDate(), time, theme, member);
    }

    private void validate(long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateRange(id);
        validate(date, time, theme, member);
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateNull(date);
        validateNull(time);
        validateNull(theme);
        validateNull(member);
    }

    private void validateRange(long id) {
        if (id <= 0) {
            throw new IllegalStateException("id는 0 이하일 수 없습니다.");
        }
    }

    private void validateNull(Object value) {
        if (value == null) {
            throw new IllegalStateException("데이터는 null일 수 없습니다.");
        }
    }

    public long getId() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme) && Objects.equals(member, that.member);
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
