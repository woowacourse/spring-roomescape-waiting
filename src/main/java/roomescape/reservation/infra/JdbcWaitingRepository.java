package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jdbcTemplate.query(
                "SELECT id, name, date, theme_id, time_id FROM waiting WHERE id = ?",
                (rs, rowNum) -> Waiting.of(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id")),
                id
        ).stream().findFirst();
    }

    @Override
    public boolean existsByNameAndDateAndThemeIdAndTimeId(String name, LocalDate date, Long themeId, Long timeId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM waiting WHERE name = ? AND date = ? AND theme_id = ? AND time_id = ?)",
                Boolean.class,
                name, date, themeId, timeId));
    }

    @Override
    public Optional<Waiting> findOldestByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId) {
        return jdbcTemplate.query(
                """
                        SELECT id, name, date, theme_id, time_id
                        FROM waiting
                        WHERE date = ? AND theme_id = ? AND time_id = ?
                        ORDER BY id ASC
                        LIMIT 1
                        """,
                (rs, rowNum) -> Waiting.of(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id")),
                date, themeId, timeId
        ).stream().findFirst();
    }

    @Override
    public List<WaitingDetail> findByName(String name) {
        return jdbcTemplate.query(
                """
                        SELECT ranked.id, ranked.name, ranked.date,
                               ranked.theme_id, t.name AS theme_name, t.description, t.thumbnail_img_url,
                               ranked.time_id, rt.start_at,
                               ranked.rank
                        FROM (
                            SELECT id, name, date, theme_id, time_id,
                                ROW_NUMBER() OVER (
                                    PARTITION BY date, theme_id, time_id
                                    ORDER BY id
                                ) AS rank
                            FROM waiting
                        ) ranked
                        JOIN theme t ON ranked.theme_id = t.id
                        JOIN reservation_time rt ON ranked.time_id = rt.id
                        WHERE ranked.name = ?
                        ORDER BY ranked.date ASC
                        """,
                (rs, rowNum) -> new WaitingDetail(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getLong("rank")),
                name
        );
    }

    @Override
    public Waiting save(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("theme_id", waiting.getThemeId())
                .addValue("time_id", waiting.getTimeId());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return waiting.withId(id);
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    @Override
    public Integer deleteOldestBySlot(LocalDate date, Long themeId, Long timeId) {
        return jdbcTemplate.update(
                """
                        DELETE FROM waiting WHERE id = (
                            SELECT id FROM (
                                SELECT id FROM waiting
                                WHERE date = ? AND theme_id = ? AND time_id = ?
                                ORDER BY id ASC LIMIT 1
                            ) AS sub
                        )
                        """,
                date, themeId, timeId
        );
    }
}
