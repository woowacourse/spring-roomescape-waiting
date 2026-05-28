package roomescape.reservationWaiting.repository;

import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.entity.ReservationWaitingEntity;
import roomescape.theme.repository.ThemeMapper;
import roomescape.time.repository.ReservationTimeMapper;

public class ReservationWaitingMapper {

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
                domain.id(),
                domain.name(),
                domain.date(),
                ReservationTimeMapper.toEntity(domain.time()),
                ThemeMapper.toEntity(domain.theme())
        );
    }
}
