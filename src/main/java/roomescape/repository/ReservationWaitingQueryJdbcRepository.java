package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.projection.ReservationWaitingWithOrder;

@Repository
public class ReservationWaitingQueryJdbcRepository implements ReservationWaitingQueryRepository {

    private static final String SELECT_BASE = """
            WITH ordered_waiting AS (
                SELECT rw.id as waiting_id, rw.name as waiting_name, rw.reservation_id,
                    ROW_NUMBER() OVER (PARTITION BY rw.reservation_id ORDER BY rw.id) as waiting_order
                FROM reservation_waiting as rw
            )
            SELECT 
                ow.waiting_id, ow.waiting_name, ow.waiting_order,
                r.date,
                t.id as time_id, t.start_at as time_value,
                th.id as theme_id, th.name as theme_name,
                th.description as theme_description,
                th.thumbnail_image_url as theme_thumbnail
            FROM ordered_waiting as ow
            INNER JOIN reservation as r ON ow.reservation_id = r.id
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingQueryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationWaitingWithOrder> waitingWithOrderRowMapper = (rs, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        return new ReservationWaitingWithOrder(
                rs.getLong("waiting_id"),
                rs.getString("waiting_name"),
                rs.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                rs.getInt("waiting_order")
        );
    };

    @Override
    public Optional<ReservationWaitingWithOrder> findById(Long id) {
        String sql = SELECT_BASE + " WHERE ow.waiting_id = ?";
        List<ReservationWaitingWithOrder> results = jdbcTemplate.query(sql, waitingWithOrderRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationWaitingWithOrder> findByName(String name) {
        String sql = SELECT_BASE + " WHERE ow.waiting_name = ? ORDER BY ow.waiting_order ASC";
        return jdbcTemplate.query(sql, waitingWithOrderRowMapper, name);
    }
}
