package roomescape.reservation.infrastructure.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;

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
    public boolean existsReservation(LocalDate date, Long timeId, Long themeId, Long memberId,
                                     ReservationStatus status) {
        return em.createQuery("""
                                SELECT EXISTS (
                            SELECT 1
                            FROM Reservation r
                            WHERE r.date = :date
                              AND r.time.id = :timeId
                              AND r.theme.id = :themeId
                              AND r.member.id = :memberId
                              AND r.status = :status
                        )
                        """, Boolean.class)
                .setParameter("date", date)
                .setParameter("timeId", timeId)
                .setParameter("themeId", themeId)
                .setParameter("memberId", memberId)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public List<ReservationWithRank> findReservationWithRankById(Long memberId) {
        return em.createQuery("""
                        SELECT new roomescape.reservation.domain.ReservationWithRank(r,
                                (SELECT COUNT(r2) FROM Reservation r2
                         WHERE r2.theme.id = r.theme.id
                            AND r2.date = r.date 
                            AND r2.time = r.time 
                            AND r2.createdAt < r.createdAt)) 
                         FROM Reservation r 
                         JOIN FETCH r.theme
                         JOIN FETCH r.time
                         WHERE r.member.id = :memberId
                        """)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    @Override
    public List<Reservation> findAllWaitingReservations(LocalDateTime now) {
        return em.createQuery("""
                        SELECT r FROM Reservation r JOIN FETCH r.time JOIN FETCH r.theme JOIN FETCH r.member
                        WHERE r.status = :status
                        AND (r.date > :nowDate OR (r.date = :nowDate AND r.time.startAt > :nowTime))
                        """, Reservation.class)
                .setParameter("nowDate", now.toLocalDate())
                .setParameter("nowTime", now.toLocalTime())
                .setParameter("status", ReservationStatus.WAITED)
                .getResultList();
    }

    private StringBuilder createQuery(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        StringBuilder query = new StringBuilder(
                "SELECT r FROM Reservation r JOIN FETCH r.theme JOIN FETCH r.member JOIN FETCH r.time WHERE 1=1");

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
