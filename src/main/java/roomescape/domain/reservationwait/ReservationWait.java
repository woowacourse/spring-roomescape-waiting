package roomescape.domain.reservationwait;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;

@Entity
public class ReservationWait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @NotNull
    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private ReservationWaitStatus status;

    public ReservationWait() {
    }

    public ReservationWait(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                           ReservationWaitStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public ReservationWait(Member member, LocalDate date, ReservationTime time, Theme theme,
                           ReservationWaitStatus status) {
        this(null, member, date, time, theme, status);
    }

    public void cancel() {
        if (status.isWaiting()) {
            status = ReservationWaitStatus.CANCELED;
        }
    }

    public void confirm() {
        if (status.isWaiting()) {
            status = ReservationWaitStatus.CONFIRMED;
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

    public ReservationWaitStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWait that = (ReservationWait) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme, status);
    }
}
