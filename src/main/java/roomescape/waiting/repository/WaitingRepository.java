package roomescape.waiting.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithOrder;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservation_idAndMember_id(Long reservation_id, Long memberId);

    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithOrder(
                w, (
                    SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w.reservation = w2.reservation
                    AND w.createdAt > w2.createdAt
                )
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithOrder> findByMember_idWithRank(Long memberId);
}
