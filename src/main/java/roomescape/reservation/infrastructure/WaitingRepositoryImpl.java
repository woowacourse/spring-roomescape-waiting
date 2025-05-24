package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.domain.WaitingWithRank;

@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    @Override
    public Waiting save(final Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public void delete(final Waiting waiting) {
        jpaWaitingRepository.delete(waiting);
    }

    @Override
    public void deleteById(final Long waitingId) {
        jpaWaitingRepository.deleteById(waitingId);
    }

    @Override
    public boolean existsById(final Long waitingId) {
        return jpaWaitingRepository.existsById(waitingId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(
            final LocalDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId) {
        return jpaWaitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                date, timeId, themeId, memberId
        );
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<WaitingWithRank> findAllWaitingWithRankByMemberId(final Long memberId) {
        return jpaWaitingRepository.findAllWaitingWithRankByMemberId(memberId);
    }

    @Override
    public List<WaitingWithRank> findAllWaitingWithRank() {
        return jpaWaitingRepository.findAllWaitingWithRank();
    }

    @Override
    public Optional<Waiting> findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt(
            final LocalDate date,
            final Long timeId,
            final Long themeId) {
        return jpaWaitingRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt(date, timeId, themeId);
    }
}
