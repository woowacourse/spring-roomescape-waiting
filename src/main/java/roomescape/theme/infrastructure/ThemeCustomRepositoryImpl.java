package roomescape.theme.infrastructure;

import jakarta.persistence.EntityManager;
import java.util.List;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.theme.domain.Theme;

public class ThemeCustomRepositoryImpl implements ThemeCustomRepository {

    private EntityManager em;

    public ThemeCustomRepositoryImpl(EntityManager em) {
        this.em = em;
    }


    @Override
    public List<Theme> findPopularThemes(ReservationPeriod period, int popularCount){
        return em.createQuery("""
                        SELECT t
                        FROM Theme as t
                        LEFT JOIN Reservation as r ON t.id = r.theme.id
                        WHERE r.date BETWEEN :start AND :end
                        GROUP BY t
                        ORDER BY count(r) DESC
                        """, Theme.class)
                .setParameter("start", period.findStartDate())
                .setParameter("end", period.findEndDate())
                .setMaxResults(popularCount)
                .getResultList();
    }
}
