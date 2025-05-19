package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

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

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.BOOKED;

    @Builder
    private Reservation(
            final Long id,
            @NonNull final LocalDate date,
            @NonNull final ReservationTime time,
            @NonNull final Theme theme,
            @NonNull final Member member,
            @NonNull final ReservationStatus status
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.status = status;
        validateFutureOrPresent();
    }

    public static Reservation booked(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member
    ) {
        return builder()
                .id(null)
                .date(date)
                .time(reservationTime)
                .theme(theme)
                .member(member)
                .status(ReservationStatus.BOOKED)
                .build();
    }

    private void validateFutureOrPresent() {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        final LocalDateTime currentDateTime = LocalDateTime.now();
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new ReservationException("예약은 현재 시간 이후로 가능합니다.");
        }
    }
}
