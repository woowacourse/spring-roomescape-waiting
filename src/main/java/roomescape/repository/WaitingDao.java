package roomescape.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<Waiting> rowMapper = (rs, rowNum) -> Waiting.create(
            rs.getLong("waiting_id"),
            new Member(rs.getString("name")),
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
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        Slot slot = waiting.slot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.owner().name())
                .addValue("date", slot.date())
                .addValue("time_id", slot.time().id())
                .addValue("theme_id", slot.theme().id())
                .addValue("created_at", waiting.createdAt());

        long id = insertExecutor.executeAndReturnKey(params).longValue();

        return Waiting.create(id, waiting.owner(), slot, waiting.createdAt());
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM waiting WHERE id = :id";
        int affected = jdbcTemplate.update(sql, Map.of("id", id));

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약 대기를 찾을 수 없습니다.");
        }
    }

    public Optional<Waiting> findById(long id) {
        String sql = SELECT_BASE + " WHERE waiting.id = :id";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper)
                .stream()
                .findFirst();
    }

    public List<Waiting> findAll() {
        return jdbcTemplate.query(SELECT_BASE, Map.of(), rowMapper);
    }

    public List<Waiting> findAllSharingSlotWith(Member member) {
        String sql = SELECT_BASE + """
                 WHERE EXISTS (
                     SELECT 1 FROM waiting as mine
                     WHERE mine.name = :name
                         AND mine.date = waiting.date
                         AND mine.time_id = waiting.time_id
                         AND mine.theme_id = waiting.theme_id
                 )
                """;
        return jdbcTemplate.query(sql, Map.of("name", member.name()), rowMapper);
    }

    public boolean existsBySlotAndOwner(Slot slot, Member member) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM waiting
                    WHERE date = :date AND time_id = :timeId AND theme_id = :themeId AND name = :name
                )
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.date())
                .addValue("timeId", slot.time().id())
                .addValue("themeId", slot.theme().id())
                .addValue("name", member.name());
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }

}
