package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

@Repository
public class JpaWaitingRepository implements WaitingRepository {

    private final WaitingListCrudRepository waitingListCrudRepository;

    public JpaWaitingRepository(WaitingListCrudRepository waitingListCrudRepository) {
        this.waitingListCrudRepository = waitingListCrudRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return waitingListCrudRepository.save(waiting);
    }

    @Override
    public List<Waiting> findAll() {
        return waitingListCrudRepository.findAll();
    }

    @Override
    public Optional<Waiting> findByIdAndMemberId(Long id, Long memberId) {
        return waitingListCrudRepository.findByIdAndMemberId(id, memberId);
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        return waitingListCrudRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return waitingListCrudRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public void deleteById(Long id) {
        waitingListCrudRepository.deleteById(id);
    }
}
