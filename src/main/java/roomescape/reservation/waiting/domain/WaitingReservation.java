package roomescape.reservation.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class WaitingReservation {

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

    @CreationTimestamp
    private LocalTime createdAt;

    public WaitingReservation(final LocalDate date,
                              final ReservationTime time,
                              final Theme theme,
                              final Member member
    ) {
        validateIsNonNull(date);
        validateIsNonNull(time);
        validateIsNonNull(theme);
        validateIsNonNull(member);

        this.id = null;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
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
