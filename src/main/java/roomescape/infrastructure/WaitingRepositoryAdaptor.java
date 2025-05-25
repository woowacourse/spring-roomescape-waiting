package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.repository.WaitingRepository;

@Repository
public class WaitingRepositoryAdaptor implements WaitingRepository {
    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryAdaptor(final JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<Waiting> findByThemeId(final Long id) {
        return jpaWaitingRepository.findByThemeId(id);
    }

    @Override
    public List<Waiting> findByMemberId(final Long memberId) {
        return jpaWaitingRepository.findByMemberId(memberId);
    }

    @Override
    public List<Waiting> findByReservationTimeId(final Long id) {
        return jpaWaitingRepository.findByReservationTimeId(id);
    }

    @Override
    public Optional<Waiting> findByDateAndReservationTimeAndThemeAndMember(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member) {
        return jpaWaitingRepository.findByDateAndReservationTimeAndThemeAndMember(date, time, theme, member);
    }

    @Override
    public List<WaitingWithRank> findByMemberIdSortedByCreateAt(final Long memberId) {
        return jpaWaitingRepository.findByMemberIdSortedByCreateAt(memberId);
    }

    @Override
    public List<WaitingWithRank> findByDateAndReservationTimeAndThemeSortedByCreateAt(final LocalDate date,
                                                                                      final Long timeId,
                                                                                      final Long themeId) {
        return jpaWaitingRepository.findByDateAndReservationTimeAndThemeSortedByCreateAt(date, timeId, themeId);
    }

    @Override
    public void deleteById(final Long id) {
        jpaWaitingRepository.deleteById(id);
    }
}
