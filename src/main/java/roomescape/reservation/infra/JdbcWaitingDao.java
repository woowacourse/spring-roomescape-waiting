package roomescape.reservation.infra;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.dao.WaitingDetailDao;
import roomescape.reservation.application.dto.WaitingDetail;

@RequiredArgsConstructor
@Repository
public class JdbcWaitingDao implements WaitingDetailDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<WaitingDetail> findByName(String username) {
        return jdbcTemplate.query(
                """
                        SELECT w.id,
                               w.name,
                               w.date,
                               w.theme_id,
                               t.name as theme_name,
                               t.description,
                               t.thumbnail_img_url,
                               w.time_id,
                               rt.start_at,
                               w.rank,
                               (
                                   SELECT COUNT(*)
                                   FROM waiting total
                                   WHERE total.date = w.date
                                     AND total.theme_id = w.theme_id
                                     AND total.time_id = w.time_id
                               ) AS total_rank_count
                        FROM waiting w
                        JOIN theme t ON w.theme_id = t.id
                        JOIN reservation_time rt ON w.time_id = rt.id
                        WHERE w.name = ?
                        ORDER BY w.date ASC, rt.start_at ASC
                        """,
                (rs, rowNum) ->
                        new WaitingDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime(),
                                rs.getInt("rank"),
                                rs.getInt("total_rank_count")),
                username
        );
    }
}
