package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import roomescape.exception.ReservationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomEscapeInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ReservationTime time;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theme theme;

    @Builder
    private RoomEscapeInformation(
            final Long id,
            @NonNull final LocalDate date,
            @NonNull final ReservationTime time,
            @NonNull final Theme theme
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    @PrePersist
    private void validateFutureOrPresent() {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        final LocalDateTime currentDateTime = LocalDateTime.now();
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new ReservationException("예약은 현재 시간 이후로 가능합니다.");
        }
    }
}
