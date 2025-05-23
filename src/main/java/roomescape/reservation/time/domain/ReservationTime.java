package roomescape.reservation.time.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.BusinessException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    public ReservationTime(final LocalTime startAt) {
        validateIsNonNull(startAt);

        this.id = null;
        this.startAt = startAt;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("시간 정보는 null 일 수 없습니다.");
        }
    }

    public boolean isBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }

    public boolean isEqual(final LocalTime time) {
        return startAt.equals(time);
    }
}
