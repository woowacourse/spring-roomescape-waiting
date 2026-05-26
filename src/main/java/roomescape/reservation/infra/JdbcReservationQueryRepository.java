package roomescape.reservation.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.repository.ReservationQueryRepository;

@Repository
public class JdbcReservationQueryRepository implements ReservationQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE time_id = ?",
                Integer.class,
                timeId
        );
        return result > 0;
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE theme_id = ?",
                Integer.class,
                themeId
        );
        return result > 0;
    }
}
