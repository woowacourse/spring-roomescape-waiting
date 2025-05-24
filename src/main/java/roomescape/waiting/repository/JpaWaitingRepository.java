package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

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
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        return waitingListCrudRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public Long countRankByCreateAt(Waiting waiting) {
        return waitingListCrudRepository.countByCreatedAtBeforeAndDateAndThemeAndTime(
                waiting.getCreatedAt(),
                waiting.getDate(),
                waiting.getTheme(),
                waiting.getTime()) + 1;
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitingListCrudRepository.findById(id);
    }

    @Override
    public void delete(Waiting waiting) {
        waitingListCrudRepository.delete(waiting);
    }

    @Override
    public boolean existsByMemberAndDateAndTime(Member member, LocalDate date, ReservationTime time) {
        return waitingListCrudRepository.existsByMemberAndDateAndTime(member, date, time);
    }
}
