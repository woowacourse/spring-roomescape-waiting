package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcSlotRepository implements SlotRepository {

    private static final RowMapper<Slot> SLOT_ROW_MAPPER = slotRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert slotInsert;

    public JdbcSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.slotInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("slot")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Slot> slotRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime()
            );
            Theme theme = Theme.of(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_image_url"),
                    resultSet.getLong("theme_running_time")
            );
            return Slot.of(
                    resultSet.getLong("slot_id"),
                    resultSet.getDate("slot_date").toLocalDate(),
                    time,
                    theme
            );
        };
    }

    @Override
    public Slot findOrCreate(LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                SELECT id
                FROM slot
                WHERE date = :date
                  AND time_id = :time_id
                  AND theme_id = :theme_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());

        try {
            long id = slotInsert.executeAndReturnKey(params).longValue();
            return Slot.of(id, date, time, theme);
        } catch (DuplicateKeyException alreadyExists) {
            Long id = jdbcTemplate.queryForObject(sql, params, Long.class);
            return Slot.of(id, date, time, theme);
        }
    }

    @Override
    public void lockForUpdate(Long slotId) {
        String sql = """
                SELECT id
                FROM slot
                WHERE id = :id FOR UPDATE
                """;

        jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("id", slotId),
                Long.class
        );
    }

    @Override
    public Optional<Slot> findById(Long id) {
        String sql = """
                SELECT s.id          AS slot_id,
                       s.date        AS slot_date,
                       t.id          AS time_id,
                       t.start_at    AS start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.image_url  AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM slot AS s
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE s.id = :id
                """;

        List<Slot> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("id", id),
                SLOT_ROW_MAPPER
        );

        return results.stream()
                .findFirst();
    }

    @Override
    public void deleteByThemeId(Long themeId) {
        String sql = """
                DELETE FROM slot
                WHERE theme_id = :theme_id
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource("theme_id", themeId));
    }

    @Override
    public void deleteByTimeId(Long timeId) {
        String sql = """
                DELETE FROM slot
                WHERE time_id = :time_id
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource("time_id", timeId));
    }
}
