package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
                SELECT w FROM Waiting w
                JOIN FETCH w.time t
                JOIN FETCH w.theme th
                WHERE w.member = :member
                ORDER BY w.createdAt
            """)
    List<Waiting> findAllByMemberOrderByCreatedAt(@Param("member") Member member);

    List<Waiting> findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt(LocalDate date, Long timeId, Long themeId);
}
