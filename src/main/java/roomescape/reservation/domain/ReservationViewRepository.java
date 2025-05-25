package roomescape.reservation.domain;

import java.util.List;

public interface ReservationViewRepository {

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId, final Long userId);

    List<ReservationView> findAllByUserId(Long userId);
}
