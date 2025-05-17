package roomescape.reservation.infrastructure.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate from,
                                                             LocalDate to) {
        StringBuilder query = createQuery(memberId, themeId, from, to);

        TypedQuery<Reservation> typedQuery = createParameter(
                memberId, themeId, from, to, query);

        return typedQuery.getResultList();
    }

    @Override
    public List<Reservation> findByMemberId(Long id) {
        return em.createQuery("""
                             SELECT r FROM Reservation r
                             JOIN FETCH r.member
                             JOIN FETCH r.theme
                             JOIN FETCH r.time
                             WHERE r.member.id = :memberId
                        """)
                .setParameter("memberId", id)
                .getResultList();
    }

    private StringBuilder createQuery(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        StringBuilder query = new StringBuilder("SELECT r FROM Reservation r WHERE 1=1");

        if (memberId != null) {
            query.append(" AND r.member.id = :memberId");
        }
        if (themeId != null) {
            query.append(" AND r.theme.id = :themeId");
        }
        if (from != null) {
            query.append(" AND r.date >= :from");
        }
        if (to != null) {
            query.append(" AND r.date <= :to");
        }
        return query;
    }

    private TypedQuery<Reservation> createParameter(Long memberId, Long themeId, LocalDate from, LocalDate to,
                                                    StringBuilder query) {
        TypedQuery<Reservation> typedQuery = em.createQuery(query.toString(), Reservation.class);

        if (memberId != null) {
            typedQuery.setParameter("memberId", memberId);
        }
        if (themeId != null) {
            typedQuery.setParameter("themeId", themeId);
        }
        if (from != null) {
            typedQuery.setParameter("from", from);
        }
        if (to != null) {
            typedQuery.setParameter("to", to);
        }
        return typedQuery;
    }
}
