package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WaitingReservationRepositoryImpl implements WaitingReservationRepository {

    private final JpaWaitingReservationRepository jpaWaitingReservationRepository;

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId) {
        return jpaWaitingReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<WaitingReservation> findAllByUserId(final Long userId) {
        return jpaWaitingReservationRepository.findAllByUserId(userId);
    }

    @Override
    public int decrementWaitingOrderAfter(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final int waitingOrder) {
        return jpaWaitingReservationRepository.decrementWaitingOrderAfter(
                time.getId(),
                theme.getId(),
                date,
                waitingOrder
        );
    }

    @Override
    public void deleteById(final Long id) {
        jpaWaitingReservationRepository.deleteById(id);
    }

    @Override
    public Optional<WaitingReservation> findById(final Long id) {
        return jpaWaitingReservationRepository.findById(id);
    }

    @Override
    public WaitingReservation save(final WaitingReservation waitingReservation) {
        return jpaWaitingReservationRepository.save(waitingReservation);
    }

    @Override
    public int findMaxWaitingByParams(final ReservationDate date, final ReservationTime time, final Theme theme) {
        return jpaWaitingReservationRepository.findMaxWaitingByParams(date, time, theme);
    }
}
