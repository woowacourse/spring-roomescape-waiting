package roomescape.reservation.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import roomescape.reservation.dto.response.WaitingWithRankResponse;

public class WaitingRepositoryImpl implements WaitingRepositoryCustom {

    private static final int WAITING_RANK_OFFSET = 1;
    private final EntityManager entityManager;

    public WaitingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<WaitingWithRankResponse> findByMemberIdWithRank(Long memberId) {
        String jpql = """
                SELECT new roomescape.reservation.dto.response.WaitingWithRankResponse(
                    w,
                    (SELECT COUNT(w2) + :offset
                     FROM Waiting w2
                     WHERE w2.theme = w.theme
                       AND w2.reservationTime = w.reservationTime
                       AND (w2.queuedAt < w.queuedAt OR (w2.queuedAt = w.queuedAt AND w2.id < w.id))
                     ))
                FROM Waiting w
                JOIN FETCH w.member
                JOIN FETCH w.theme
                JOIN FETCH w.reservationTime.timeSlot
                WHERE w.member.id = :memberId
                """;

        TypedQuery<WaitingWithRankResponse> query = entityManager.createQuery(jpql, WaitingWithRankResponse.class);
        query.setParameter("memberId", memberId);
        query.setParameter("offset", WAITING_RANK_OFFSET);
        return query.getResultList();
    }
}
