package roomescape.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class WaitingDao {

    private static final String SELECT_BASE = """
            SELECT
                waiting.id as waiting_id,
                waiting.name,
                waiting.date,
                time.id as time_id,
                time.start_at as time_value,
                theme.id as theme_id,
                theme.name as theme_name,
                theme.thumbnail_url as thumbnail_url,
                theme.description as theme_description,
                waiting.created_at as created_at
            FROM waiting as waiting
            INNER JOIN reservation_time as time ON waiting.time_id = time.id
            INNER JOIN theme as theme ON waiting.theme_id = theme.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<Waiting> rowMapper = (rs, rowNum) -> Waiting.create(
            rs.getLong("waiting_id"),
            rs.getString("name"),
            mapSlot(rs),
            rs.getObject("created_at", LocalDateTime.class)
    );

    private static Slot mapSlot(ResultSet rs) throws SQLException {
        Theme theme = Theme.create(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.create(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return new Slot(rs.getObject("date", LocalDate.class), reservationTime, theme);
    }

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        Slot slot = waiting.slot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.name())
                .addValue("date", slot.date())
                .addValue("time_id", slot.time().id())
                .addValue("theme_id", slot.theme().id())
                .addValue("created_at", waiting.createAt());

        long id = insertExecutor.executeAndReturnKey(params).longValue();

        return Waiting.create(id, waiting.name(), slot, waiting.createAt());
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM waiting WHERE id = ?";
        int affected = jdbcTemplate.update(sql, id);

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약 대기를 찾을 수 없습니다.");
        }
    }

    public Optional<Waiting> findById(long id) {
        String sql = SELECT_BASE + " WHERE waiting.id = ?";
        return jdbcTemplate.query(sql, rowMapper, id)
                .stream()
                .findFirst();
    }

    public List<Waiting> findAll() {
        return jdbcTemplate.query(SELECT_BASE, rowMapper);
    }

    public boolean existsBySlotAndName(Slot slot, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM waiting
                    WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
                )
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, slot.date(), slot.time().id(), slot.theme().id(), name)
        );
    }

}
