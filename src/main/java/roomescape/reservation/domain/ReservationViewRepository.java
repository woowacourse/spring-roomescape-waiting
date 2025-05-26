package roomescape.reservation.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationViewRepository {

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId, final Long userId);

    List<ReservationView> findAllByUserId(Long userId);

    Optional<Long> findFirstWaitingByReservationId(Long id);
}
