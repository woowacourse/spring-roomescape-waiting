package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.jpa.ReservationWaitingJpaRepository;

@Repository
public class ReservationWaitingRepositoryImpl implements
        ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;

    public ReservationWaitingRepositoryImpl(final ReservationWaitingJpaRepository reservationWaitingJpaRepository) {
        this.reservationWaitingJpaRepository = reservationWaitingJpaRepository;
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        return reservationWaitingJpaRepository.save(reservationWaiting);
    }

    @Override
    public Optional<ReservationWaiting> findByThemeIdAndTimeIdAndDate(final long themeId, final long timeId,
                                                                      final LocalDate date) {
        return reservationWaitingJpaRepository.findByThemeIdAndTimeIdAndDate(themeId, timeId, date);
    }

    @Override
    public void deleteById(final long id) {
        reservationWaitingJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(final long id) {
        return reservationWaitingJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(final long memberId, final long themeId,
                                                              final long timeId, final LocalDate date) {
        return reservationWaitingJpaRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(memberId, themeId, timeId, date);
    }

    @Override
    public List<ReservationWaiting> findByMemberId(final long memberId) {
        return reservationWaitingJpaRepository.findByMemberId(memberId);
    }

    @Override
    public int findWaitingOrderById(final long id) {
        return reservationWaitingJpaRepository.findWaitingOrderById(id);
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return reservationWaitingJpaRepository.findAll();
    }

    @Override
    public Optional<ReservationWaiting> findFirstByThemeIdAndTimeIdAndDateOrderByCreatedAtAsc(final long themeId,
                                                                                              final long timeId,
                                                                                              final LocalDate date) {
        return reservationWaitingJpaRepository.findFirstByThemeIdAndTimeIdAndDateOrderByCreatedAtAsc(themeId, timeId, date);
    }
}
