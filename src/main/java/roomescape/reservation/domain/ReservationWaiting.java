package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Embedded
    @Column(nullable = false)
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ReservationWaiting() {
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
    }

    public void updateStatus(Status status) {
        this.status = status;
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
