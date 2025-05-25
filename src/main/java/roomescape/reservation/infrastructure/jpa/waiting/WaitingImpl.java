package roomescape.reservation.infrastructure.jpa.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingRepository;
import roomescape.reservation.domain.waiting.WaitingWithRank;

@Repository
public class WaitingImpl implements WaitingRepository {

    private final WaitingJpaRepository waitingJpaRepository;

    public WaitingImpl(final WaitingJpaRepository waitingJpaRepository) {
        this.waitingJpaRepository = waitingJpaRepository;
    }

    @Override
    public boolean existsByReservation(final LocalDate date, final long timeId, final long themeId) {
        return waitingJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public     boolean existsByReservationAndMemberId(final LocalDate date, final long timeId, final long themeId, final long memberId) {
        return waitingJpaRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public Waiting save(final Waiting waiting) {
       return waitingJpaRepository.save(waiting);
    }

    @Override
    public void deleteById(final long id) {
        waitingJpaRepository.deleteById(id);
    }

    @Override
    public List<Waiting> findAll() {
        return waitingJpaRepository.findAll();
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        return waitingJpaRepository.findWaitingsWithRankByMemberId(memberId);
    }

    @Override
    public Optional<Waiting> findTopByReservation(final LocalDate date, final long timeId,
                                                  final long themeId) {
        return waitingJpaRepository.findTopByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
