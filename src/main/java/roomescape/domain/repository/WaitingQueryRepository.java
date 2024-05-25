package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRankDto;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface WaitingQueryRepository extends Repository<Waiting, Long> {

    @Query("""
            select new roomescape.domain.dto.WaitingWithRankDto(
            w,
                cast((select count(w2)
                from Waiting w2
                where w2.date = w.date
                and w2.time = w.time
                and w2.theme = w.theme
                and w2.id < w.id)
                as long)
            )
            from Waiting w
            where w.member.id = :memberId
            """)
    List<WaitingWithRankDto> findWaitingWithRankByMemberId(Long memberId);

    boolean existsByMemberAndDateAndTimeAndTheme(Member member, LocalDate date, Time time, Theme theme);

    boolean existsByMemberAndDateAndTime(Member member, LocalDate date, Time time);

    Optional<Waiting> findById(Long id);

    List<Waiting> findAll();

    @Query("""
            select w
            from Waiting w
            where w.date = :date
            and w.time = :time
            and w.theme = :theme
            order by w.id
            limit 1
            """)
    Optional<Waiting> findFirstWaiting(LocalDate date, Time time, Theme theme);

    default Waiting getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_WAITING,
                String.format("존재하지 않는 예약 대기입니다. 요청 예약 대기 id:%d", id)));
    }
}
