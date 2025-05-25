package roomescape.reservation.infrastructure.jpa.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

@Repository
public class ReservationWaitingImpl implements ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;

    public ReservationWaitingImpl(final ReservationWaitingJpaRepository reservationWaitingJpaRepository) {
        this.reservationWaitingJpaRepository = reservationWaitingJpaRepository;
    }

    @Override
    public boolean existsByReservation(final LocalDate date, final long timeId, final long themeId) {
        return reservationWaitingJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public     boolean existsByReservationAndMemberId(final LocalDate date, final long timeId, final long themeId, final long memberId) {
        return reservationWaitingJpaRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
       return reservationWaitingJpaRepository.save(reservationWaiting);
    }

    @Override
    public void deleteById(final long id) {
        reservationWaitingJpaRepository.deleteById(id);
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return reservationWaitingJpaRepository.findAll();
    }

    @Override
    public List<ReservationWaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        return reservationWaitingJpaRepository.findWaitingsWithRankByMemberId(memberId);
    }

    @Override
    public Optional<ReservationWaiting> findTopByReservation(final LocalDate date, final long timeId,
                                                             final long themeId) {
        return reservationWaitingJpaRepository.findTopByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
