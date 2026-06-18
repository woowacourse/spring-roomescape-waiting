package roomescape.reservation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column
    private LocalDateTime createdAt;

    public boolean isReserved() {
        return this.status.equals(Status.RESERVED);
    }

    public void validateChangeableBy(String name, LocalDateTime dateTime) {
        validateOwnedBy(name);
        time.validateExpired(dateTime);
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenRequestException();
        }
    }

    public Reservation(String name, ReservationTime time, Theme theme, Status status, LocalDateTime createdAt) {
        this.name = name;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void validateExpired(LocalDateTime dateTime) {
        time.validateExpired(dateTime);
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public void promote() {
        if (this.status != Status.WAITING) {
            throw new IllegalStateException("WAITING 상태만 예약으로 가능합니다.");
        }
        status = Status.RESERVED;
    }

    public void update(ReservationTime newTime) {
        time = newTime;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
