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
    public Optional<Waiting> findById(Long id) {
        return waitingListCrudRepository.findById(id);
    }

    @Override
    public Optional<Waiting> findByIdAndMemberId(Long id, Long memberId) {
        return waitingListCrudRepository.findByIdAndMemberId(id, memberId);
    }

    @Override
    public Optional<Waiting> findFirstWaitingByDetails_DateAndDetails_Time_IdAndDetails_Theme_Id(LocalDate date, Long timeId, Long themeId) {
        return waitingListCrudRepository.findFirstWaitingByDetails_DateAndDetails_Time_IdAndDetails_Theme_Id(date, timeId, themeId);
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        return waitingListCrudRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public boolean existsByDetails_DateAndDetails_Time_IdAndDetails_Theme_IdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return waitingListCrudRepository.existsByDetails_DateAndDetails_Time_IdAndDetails_Theme_IdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public void deleteById(Long id) {
        waitingListCrudRepository.deleteById(id);
    }
}
