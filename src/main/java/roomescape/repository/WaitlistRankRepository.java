package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Waitlist;
import roomescape.domain.WaitlistWithRank;

public interface WaitlistRankRepository extends JpaRepository<Waitlist, Long> {

    @Query("""
        SELECT w,
               (
                   SELECT COUNT(w2) + 1
                   FROM Waitlist w2
                   WHERE w2.slot = w.slot
                     AND (
                         w2.createdAt < w.createdAt
                         OR (w2.createdAt = w.createdAt AND w2.id < w.id)
                     )
               )
        FROM Waitlist w
        JOIN FETCH w.member
        JOIN FETCH w.slot s
        JOIN FETCH s.time t
        JOIN FETCH s.theme
        WHERE w.member.name = :name
        ORDER BY s.date DESC, t.startAt ASC
        """)
    List<Object[]> findRowsByMemberNameWithRank(@Param("name") String name);

    default List<WaitlistWithRank> findByMemberNameWithRank(String name) {
        return findRowsByMemberNameWithRank(name).stream()
            .map(row -> new WaitlistWithRank((Waitlist) row[0], ((Number) row[1]).longValue()))
            .toList();
    }
}
