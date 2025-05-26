package roomescape.reservation.repository.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.JpaWaitingRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.repository.dto.WaitingWithRankDto;

@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    @Override
    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId) {
        return jpaWaitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId) {
        return jpaWaitingRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public Optional<Waiting> findEarliestByParams(ReservationDate date, Long timeId, Long themeId) {
        return jpaWaitingRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(date, timeId, themeId);
    }

    @Override
    public void deleteById(Long id) {
        jpaWaitingRepository.deleteById(id);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public List<WaitingWithRankDto> findWithRankByMemberId(Long memberId) {
        return jpaWaitingRepository.findWithRankByMemberId(memberId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }
}
