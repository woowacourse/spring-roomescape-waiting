package roomescape.reservation.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationCondition;

public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    @PersistenceContext
    public EntityManager entityManager;

    @Override
    public List<Reservation> findByCondition(ReservationCondition condition) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
        Root<Reservation> reservation = cq.from(Reservation.class);
        reservation.fetch("member");
        reservation.fetch("theme");
        reservation.fetch("timeSlot");

        List<Predicate> predicates = new ArrayList<>();

        if (condition.memberId() != null) {
            predicates.add(cb.equal(reservation.get("member").get("id"), condition.memberId()));
        }
        if (condition.themeId() != null) {
            predicates.add(cb.equal(reservation.get("theme").get("id"), condition.themeId()));
        }
        if (condition.dateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(reservation.get("date"), condition.dateFrom()));
        }
        if (condition.dateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(reservation.get("date"), condition.dateTo()));
        }

        cq.select(reservation)
                .where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(cq).getResultList();
    }
}
