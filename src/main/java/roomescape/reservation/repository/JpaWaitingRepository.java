package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;

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
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return waitingListCrudRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }
}
