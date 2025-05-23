package roomescape.waiting.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public interface WaitingListCrudRepository extends ListCrudRepository<Waiting, Long> {

    List<Waiting> findAllByMemberId(Long memberId);


    Long countByCreatedAtBeforeAndDateAndThemeAndTime(LocalDateTime createAt, LocalDate date, Theme theme,
                                                     ReservationTime time);

    boolean existsByMemberAndDateAndTime(Member member, LocalDate date, ReservationTime time);
}
