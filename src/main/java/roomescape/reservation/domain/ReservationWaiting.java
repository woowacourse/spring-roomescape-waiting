package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationWaiting {

    private final Long id;

    private final Member member;

    private final ReservationDate date;

    private final ReservationTime time;

    private final Theme theme;

    private final Status status;

    private final LocalDateTime createdAt;

    private final Long rank;


    public ReservationWaiting(Long id,
                              Member member,
                              LocalDate date,
                              ReservationTime time,
                              Theme theme,
                              Status status,
                              LocalDateTime createdAt,
                              Long rank) {
        this.id = id;
        this.member = member;
        this.date = new ReservationDate(date);
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
        this.rank = rank;
    }

    public ReservationWaiting(Long id,
                              Member member,
                              LocalDate date,
                              ReservationTime time,
                              Theme theme,
                              Status status,
                              LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.date = new ReservationDate(date);
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
        this.rank = null;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme) && status == that.status && Objects.equals(
                createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme, status, createdAt);
    }
}
