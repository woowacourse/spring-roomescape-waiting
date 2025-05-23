package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    List<Waiting> findAllByMemberId(Long memberId);

    Optional<Waiting> findById(Long id);

    boolean existsByMemberAndDateAndTime(Member member, LocalDate date, ReservationTime time);

    Long countRankByCreateAt(Waiting waiting);

    void delete(Waiting waiting);
}
