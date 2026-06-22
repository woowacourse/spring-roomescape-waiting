package roomescape.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaitingQueryRepository;
import roomescape.domain.Theme;
import roomescape.domain.projection.ReservationWaitingWithOrder;

@Repository
public class ReservationWaitingQueryJdbcRepository implements ReservationWaitingQueryRepository {

    private static final String SELECT_WAITING_WITH_ORDER = """
            SELECT
                ow.waiting_id, ow.waiting_name, ow.waiting_order, ow.date,
                t.id as time_id, t.start_at as time_value,
                th.id as theme_id, th.name as theme_name,
                th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM ordered_waiting ow
            INNER JOIN reservation_time t ON ow.time_id = t.id
            INNER JOIN theme th ON ow.theme_id = th.id
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
        String sql = """
                WITH target_waiting AS (
                    SELECT id, date, time_id, theme_id
                    FROM reservation_waiting
                    WHERE id = ?
                ),
                ordered_waiting AS (
                    SELECT 
                        rw.id as waiting_id,
                        rw.name as waiting_name,
                        rw.date,
                        rw.time_id,
                        rw.theme_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY rw.date, rw.time_id, rw.theme_id
                            ORDER BY rw.id ASC
                        ) as waiting_order
                    FROM reservation_waiting rw
                    INNER JOIN target_waiting tw
                        ON rw.date = tw.date
                        AND rw.time_id = tw.time_id
                        AND rw.theme_id = tw.theme_id
                )
                """
                + SELECT_WAITING_WITH_ORDER
                + """
                INNER JOIN target_waiting tw ON ow.waiting_id = tw.id
                """;
        List<ReservationWaitingWithOrder> results = jdbcTemplate.query(sql, waitingWithOrderRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationWaitingWithOrder> findByMember(Member member) {
        String sql = """
                WITH my_waiting AS (
                    SELECT id, date, time_id, theme_id
                    FROM reservation_waiting
                    WHERE name = ?
                ),
                ordered_waiting AS (
                    SELECT 
                        rw.id as waiting_id,
                        rw.name as waiting_name,
                        rw.date,
                        rw.time_id,
                        rw.theme_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY rw.date, rw.time_id, rw.theme_id
                            ORDER BY rw.id ASC
                        ) as waiting_order
                    FROM reservation_waiting rw
                    INNER JOIN my_waiting mw
                        ON rw.date = mw.date
                        AND rw.time_id = mw.time_id
                        AND rw.theme_id = mw.theme_id
                )
                """
                + SELECT_WAITING_WITH_ORDER
                + """ 
                INNER JOIN my_waiting mw ON ow.waiting_id = mw.id
                ORDER BY ow.date DESC, t.start_at ASC, ow.waiting_order ASC
                """;
        return jdbcTemplate.query(sql, waitingWithOrderRowMapper, member.name());
    }
}
