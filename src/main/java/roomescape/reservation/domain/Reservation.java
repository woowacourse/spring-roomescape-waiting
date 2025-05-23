package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    public Reservation(final LocalDate date,
                       final ReservationTime time,
                       final Theme theme,
                       final Member member,
                       final ReservationStatus reservationStatus
    ) {
        validateIsNonNull(date);
        validateIsNonNull(time);
        validateIsNonNull(theme);
        validateIsNonNull(member);
        validateIsNonNull(reservationStatus);

        this.id = null;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.reservationStatus = reservationStatus;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("예약 정보는 null 일 수 없습니다.");
        }
    }

    public boolean isCannotReserveDateTime(final LocalDateTime dateTime) {
        if (date.isBefore(dateTime.toLocalDate())) {
            return true;
        }

        return date.isEqual(dateTime.toLocalDate()) && time.isBefore(dateTime.toLocalTime());
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.isEqual(time.getStartAt());
    }
}
