package roomescape.time.repository;

import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.entity.ReservationTimeEntity;

public class ReservationTimeMapper {

    private ReservationTimeMapper() {
    }

    public static ReservationTime toDomain(ReservationTimeEntity entity) {
        return new ReservationTime(
                entity.getId(),
                entity.getStartAt()
        );
    }

    public static ReservationTimeEntity toEntity(ReservationTime domain) {
        return new ReservationTimeEntity(
                domain.getId(),
                domain.getStartAt()
        );
    }
}
