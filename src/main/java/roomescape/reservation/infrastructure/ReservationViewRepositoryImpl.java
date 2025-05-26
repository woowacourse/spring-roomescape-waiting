package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationView;
import roomescape.reservation.domain.ReservationViewRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReservationViewRepositoryImpl implements ReservationViewRepository {

    private final JpaReservationViewRepository jpaReservationViewRepository;

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId, final Long userId) {
        return jpaReservationViewRepository.existsByDateAndTimeIdAndThemeIdAndUserId(date, timeId, themeId, userId);
    }

    @Override
    public List<ReservationView> findAllByUserId(final Long userId) {
        return jpaReservationViewRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Long> findFirstWaitingByReservationId(final Long id) {
        return jpaReservationViewRepository.findFirstWaitingByReservationId(id);
    }
}
