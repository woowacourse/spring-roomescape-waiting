package roomescape.repository.reservationwaiting;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {
    private static final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (resultSet, rowNumber) -> {
        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );

        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        ReservationSlot slot = new ReservationSlot(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                reservationTime
        );

        return ReservationWaiting.of(
                resultSet.getLong("id"),
                slot,
                resultSet.getString("waiting_name"),
                resultSet.getTimestamp("requested_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (slot_id, name, requested_at) VALUES (?, ?, ?) ";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setLong(1, reservationWaiting.getSlot().getId());
                preparedStatement.setString(2, reservationWaiting.getName());
                preparedStatement.setTimestamp(3, Timestamp.valueOf(reservationWaiting.getRequestedAt()));
                return preparedStatement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 대기 ID를 생성하지 못했습니다.");
        }

        return reservationWaiting.withId(key.longValue());
    }

    @Override
    public Optional<ReservationWaiting> findById(final Long id) {
        String sql = """
                SELECT rw.id,
                       rw.name AS waiting_name,
                       rw.requested_at,
                       rw.slot_id,
                       s.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation_waiting AS rw
                INNER JOIN reservation_slot AS s ON rw.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE rw.id = ?
                """;

        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public ReservationWaitingLine findLineBySlot(final ReservationSlot slot) {
        String sql = """
                SELECT rw.id,
                       rw.name AS waiting_name,
                       rw.requested_at,
                       rw.slot_id,
                       s.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation_waiting AS rw
                INNER JOIN reservation_slot AS s ON rw.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE rw.slot_id = ?
                """;

        return ReservationWaitingLine.fromWaitings(jdbcTemplate.query(
                sql,
                reservationWaitingRowMapper,
                slot.getId()
        ));
    }

    @Override
    public void delete(final ReservationWaiting reservationWaiting) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?";
        jdbcTemplate.update(sql, reservationWaiting.getId());
    }
}
