package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    @JoinColumn(name = "status_id", nullable = false)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ReservationStatus reservationStatus;

    @Builder
    private Reservation(
            final Long id,
            @NonNull final LocalDate date,
            @NonNull final ReservationTime time,
            @NonNull final Theme theme,
            @NonNull final Member member,
            @NonNull final ReservationStatus reservationStatus,
            @NonNull final LocalDateTime currentDateTime
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.reservationStatus = reservationStatus;
        validateFutureOrPresent(currentDateTime);
    }

    public static Reservation of(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member,
            final LocalDateTime currentDateTime
    ) {
        return builder()
                .id(null)
                .date(date)
                .time(reservationTime)
                .theme(theme)
                .member(member)
                .currentDateTime(currentDateTime)
                .reservationStatus(ReservationStatus.booked())
                .build();
    }

    public static Reservation waiting(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member,
            final LocalDateTime currentDateTime,
            final Long rank
    ) {
            return builder()
                    .id(null)
                    .date(date)
                    .time(reservationTime)
                    .theme(theme)
                    .member(member)
                    .currentDateTime(currentDateTime)
                    .reservationStatus(ReservationStatus.waiting(rank))
                    .build();
    }

    private void validateFutureOrPresent(LocalDateTime currentDateTime) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new ReservationException("예약은 현재 시간 이후로 가능합니다.");
        }
    }
}
