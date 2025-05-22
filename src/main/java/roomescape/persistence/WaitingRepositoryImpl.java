package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.domain.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryImpl(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(final Long memberId) {
        return jpaWaitingRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public void deleteById(final Long id) {
        jpaWaitingRepository.deleteById(id);
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndThemeId(final Long memberId, final LocalDate date, final Long timeId, final Long themeId) {
        return jpaWaitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public boolean existsById(final Long waitingId) {
        return jpaWaitingRepository.existsById(waitingId);
    }

    @Override
    public List<Waiting> findByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long timeId) {
        return jpaWaitingRepository.findByThemeIdAndDateAndTimeId(themeId, date, timeId);
    }
}
