package roomescape.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

public class ReservationRepositoryImpl implements ReservationCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo) {
        String jpql = """
                    SELECT r FROM Reservation r
                    JOIN r.time t
                    JOIN r.theme tm
                    JOIN r.member m
                    WHERE (:memberId IS NULL OR r.member.id = :memberId)
                    AND (:themeId IS NULL OR r.theme.id = :themeId)
                    AND (:dateFrom IS NULL OR r.date >= :dateFrom)
                    AND (:dateTo IS NULL OR r.date <= :dateTo)
                    ORDER BY r.id
                """;

        TypedQuery<Reservation> query = em.createQuery(jpql, Reservation.class);
        query.setParameter("memberId", memberId);
        query.setParameter("themeId", themeId);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);

        return query.getResultList();
    }
}
