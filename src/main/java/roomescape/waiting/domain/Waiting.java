package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "waiting")
@EntityListeners(AuditingEntityListener.class)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    @NotNull
    private LocalDate date;

    @JoinColumn(name = "member_id")
    @ManyToOne
    @NotNull
    private Member member;

    @JoinColumn(name = "theme_id")
    @ManyToOne
    @NotNull
    private Theme theme;

    @JoinColumn(name = "time_id")
    @ManyToOne
    @NotNull
    private ReservationTime time;

    @Column(name = "created_at")
    @CreatedDate
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private WaitingStatus status;

    protected Waiting() {
    }

    public Waiting(Long id, LocalDate date, Member member, Theme theme, ReservationTime time, WaitingStatus status) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.theme = theme;
        this.time = time;
        this.status = status;
    }

    public Waiting(LocalDate date, Member member, Theme theme, ReservationTime time, WaitingStatus status) {
        this.date = date;
        this.member = member;
        this.theme = theme;
        this.time = time;
        this.status = status;
    }

    public void updateWaiting(WaitingStatus waitingStatus) {
        if (this.status == WaitingStatus.PENDING && waitingStatus != WaitingStatus.PENDING) {
            this.status = waitingStatus;
            return;
        }
        throw new BadRequestException(ExceptionCause.WAITING_STATUS_ALREADY_UPDATED);
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

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public WaitingStatus getStatus() {
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
        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
