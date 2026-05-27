package roomescape.reservationWaiting.repository;

import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.entity.ReservationWaitingEntity;
import roomescape.theme.repository.ThemeMapper;
import roomescape.time.repository.ReservationTimeMapper;

public class ReservationWaitingMapper {

    private ReservationWaitingMapper() {
    }

    public static ReservationWaiting toDomain(ReservationWaitingEntity entity) {
        return new ReservationWaiting(
                entity.getId(),
                entity.getName(),
                entity.getDate(),
                ReservationTimeMapper.toDomain(entity.getTime()),
                ThemeMapper.toDomain(entity.getTheme())
        );
    }

    public static ReservationWaitingEntity toEntity(ReservationWaiting domain) {
        return new ReservationWaitingEntity(
                domain.getId(),
                domain.getName(),
                domain.getDate(),
                ReservationTimeMapper.toEntity(domain.getTime()),
                ThemeMapper.toEntity(domain.getTheme())
        );
    }
}
