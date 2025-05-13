package roomescape.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationRepositoryImpl implements ReservationCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo) {

        String baseSelectSql = """
                        SELECT
                            r.id ,
                            r.date,
                            t.id ,
                            t.start_at ,
                            tm.id ,
                            tm.name ,
                            tm.description ,
                            tm.thumbnail,
                            m.id,
                            m.role,
                            m.name ,
                            m.email ,
                            m.password
                        FROM reservation as r
                            inner join reservation_time as t on r.time_id = t.id
                            inner join theme as tm on r.theme_id = tm.id
                            inner join member as m on r.member_id = m.id
                        """;


        StringBuilder sql = new StringBuilder(baseSelectSql);
        boolean isFirstCondition = true;

        if (memberId != null || themeId != null || dateFrom != null || dateTo != null) {
            sql.append(" WHERE");
        }

        Map<String, String> params= new HashMap<>();
        if (memberId != null) {
            params.put("memberId", memberId.toString());
            sql.append(" r.member_id = :memberId");
            isFirstCondition = false;
        }

        if (themeId != null) {
            params.put("themeId", themeId.toString());
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.theme_id = ?");
            isFirstCondition = false;
        }

        if (dateFrom != null) {
            params.put("dateFrom", dateFrom.toString());
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.date >= :dateFrom");
            isFirstCondition = false;
        }

        if (dateTo != null) {
            params.put("dateTo", dateTo.toString());
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.date <= :dateTo");
        }

        sql.append(" ORDER BY r.id");

        TypedQuery<Reservation> query = em.createQuery(sql.toString(), Reservation.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

        private void appendAndIfNotFirstCondition(final boolean isFirstCondition, final StringBuilder sql) {
        if (!isFirstCondition) {
            sql.append(" AND");
        }
    }
}
