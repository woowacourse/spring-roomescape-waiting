package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.entity.ReservationEntity;
import roomescape.theme.repository.ThemeMapper;
import roomescape.time.repository.ReservationTimeMapper;

public class ReservationMapper {

    public static Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getName(),
                entity.getDate(),
                ReservationTimeMapper.toDomain(entity.getTime()),
                ThemeMapper.toDomain(entity.getTheme())
        );
    }

    public static ReservationEntity toEntity(Reservation domain) {
        return new ReservationEntity(
                domain.getId(),
                domain.getName(),
                domain.getDate(),
                ReservationTimeMapper.toEntity(domain.getTime()),
                ThemeMapper.toEntity(domain.getTheme())
        );
    }
}
