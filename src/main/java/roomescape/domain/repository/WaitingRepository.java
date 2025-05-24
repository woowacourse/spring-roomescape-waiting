package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<Waiting> findByMemberId(Long id);

    List<Waiting> findByThemeId(Long id);

    List<Waiting> findByReservationTimeId(Long id);

    void deleteById(Long id);

    Optional<Waiting> findByDateAndReservationTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme,
                                                                    Member member);

    List<WaitingWithRank> findByMemberIdSortedByCreateAt(Long memberId);
}
