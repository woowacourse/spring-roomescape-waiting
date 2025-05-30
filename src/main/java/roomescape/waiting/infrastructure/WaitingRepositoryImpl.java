package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private final WaitingJpaRepository waitingJpaRepository;

    public WaitingRepositoryImpl(final WaitingJpaRepository waitingJpaRepository) {
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
