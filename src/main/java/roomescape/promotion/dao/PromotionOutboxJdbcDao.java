package roomescape.promotion.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.promotion.OutboxStatus;
import roomescape.promotion.PromotionOutboxDao;
import roomescape.promotion.PromotionTask;

@Repository
public class PromotionOutboxJdbcDao implements PromotionOutboxDao {

    private static final RowMapper<PromotionTask> ROW_MAPPER = (rs, rowNum) -> PromotionTask.reconstruct(
            rs.getLong("id"),
            rs.getLong("theme_id"),
            rs.getLong("time_id"),
            LocalDate.parse(rs.getString("date")),
            rs.getLong("store_id"),
            OutboxStatus.valueOf(rs.getString("status"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public PromotionOutboxJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("promotion_outbox")
                .usingGeneratedKeyColumns("id")
                .usingColumns("date", "time_id", "theme_id", "store_id", "status");
    }

    @Override
    public PromotionTask insert(PromotionTask task) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", task.getDate())
                .addValue("time_id", task.getTimeId())
                .addValue("theme_id", task.getThemeId())
                .addValue("store_id", task.getStoreId())
                .addValue("status", task.getStatus().name());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return task.withId(id);
    }

    @Override
    public List<PromotionTask> findByStatus(OutboxStatus status) {
        String sql = """
                SELECT id, date, time_id, theme_id, store_id, status
                FROM promotion_outbox
                WHERE status = :status
                ORDER BY id
                """;
        SqlParameterSource params = new MapSqlParameterSource("status", status.name());
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    @Override
    public void markDone(Long id) {
        String sql = "UPDATE promotion_outbox SET status = :status WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", id)
                .addValue("status", OutboxStatus.DONE.name());
        jdbcTemplate.update(sql, params);
    }
}
