package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationWaitingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_RESERVATION_WAITING_SQL = """
            SELECT
                w.id,
                w.name,
                w.created_at,
                s.id         as slot_id,
                s.date       as slot_date,
                t.id         as time_id,
                t.start_at   as time_start_at,
                th.id          as theme_id,
                th.name        as theme_name,
                th.description as theme_description,
                th.url         as theme_url,
                ranked.sequence
            FROM waiting w
            JOIN slot s on w.slot_id = s.id
            JOIN reservation_time t  ON s.time_id  = t.id
            JOIN theme th            ON s.theme_id = th.id
            JOIN (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        PARTITION BY slot_id
                        ORDER BY created_at, id
                    ) as sequence
                FROM waiting
            ) as ranked ON w.id = ranked.id
            """;

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_start_at", LocalTime.class)
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );
        Slot slot = Slot.restore(
                resultSet.getLong("slot_id"),
                resultSet.getObject("slot_date", LocalDate.class),
                time,
                theme
        );
        return ReservationWaiting.restore(
                resultSet.getLong("id"),
                slot,
                resultSet.getString("name"),
                resultSet.getLong("sequence"),
                resultSet.getObject("created_at", LocalDateTime.class)
        );
    };

    public Optional<ReservationWaiting> findFirstBySlotId(Long slotId) {
        String sql = SELECT_RESERVATION_WAITING_SQL + """
                WHERE w.slot_id = ?
                ORDER BY w.created_at, w.id
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, slotId).stream()
                .findFirst();
    }

    public boolean isExistByNameAndSlotId(String name, Long slotId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM waiting
                    WHERE name = ?
                    AND slot_id = ?
            )
            """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name, slotId);
    }

    public Optional<ReservationWaiting> findReservationWaitingById(long id) {
        String sql = SELECT_RESERVATION_WAITING_SQL + " where w.id = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id).stream()
                .findFirst();
    }

    public List<ReservationWaiting> findAllReservationWaiting() {
        return jdbcTemplate.query(SELECT_RESERVATION_WAITING_SQL, reservationWaitingRowMapper);
    }

    public List<ReservationWaiting> findAllByName(String name) {
        String sql = SELECT_RESERVATION_WAITING_SQL + " where w.name = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, name);
    }

    public Long create(ReservationWaiting reservationWaiting) {
        String sql = "insert into waiting(slot_id, name, created_at) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservationWaiting.getSlot().getId());
            ps.setString(2, reservationWaiting.getName());
            ps.setObject(3, reservationWaiting.getCreatedAt());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public long delete(Long id) {
        String sql = "delete from waiting where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
