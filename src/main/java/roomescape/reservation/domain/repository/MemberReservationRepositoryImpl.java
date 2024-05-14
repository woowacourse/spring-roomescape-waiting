package roomescape.reservation.domain.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.MemberReservation;

public class MemberReservationRepositoryImpl implements MemberReservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MemberReservation> findBy(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT mr.id AS member_reservation_id, r.id AS reservation_id, r.date, t.id AS time_id, t.start_at AS time_value,
                th.id AS theme_id, th.name AS theme_name, th.description, th.thumbnail, m.id AS member_id, m.name AS member_name
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN member_reservation AS mr ON mr.reservation_id = r.id
                INNER JOIN member AS m ON m.id = mr.member_id
                """;

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        addCondition(memberId, conditions, "m.id = :memberId", params);
        addCondition(themeId, conditions, "th.id = :themeId", params);
        addCondition(startDate, conditions, "r.date >= :startDate", params);
        addCondition(endDate, conditions, "r.date <= :endDate", params);

        if (!conditions.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", conditions);
        }

        sql += " ORDER BY r.date, t.start_at;";
        Query query = entityManager.createNativeQuery(sql, MemberReservation.class);
        addParameter(query, "memberId", memberId);
        addParameter(query, "themeId", themeId);
        addParameter(query, "startDate", startDate);
        addParameter(query, "endDate", endDate);

        return query.getResultList();
    }

    private void addCondition(Object param, List<String> conditions, String sql, List<Object> params) {
        if (param == null) {
            return;
        }

        conditions.add(sql);
        params.add(param);
    }

    private void addParameter(Query query, String name, Object param) {
        if (param == null) {
            return;
        }

        query.setParameter(name, param);
    }
}
