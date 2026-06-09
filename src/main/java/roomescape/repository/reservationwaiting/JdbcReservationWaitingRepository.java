package roomescape.repository.reservationwaiting;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {

    private static final RowMapper<ReservationWaiting> waitingRowMapper = (resultSet, rowNumber) -> {
        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );
        ReservationTime time = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Reservation reservation = Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                time
        );
        return ReservationWaiting.createNew(
                        reservation,
                        resultSet.getString("waiting_name"),
                        resultSet.getTimestamp("requested_at").toLocalDateTime()
                )
                .withId(resultSet.getLong("waiting_id"));
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (reservation_id, name, requested_at) VALUES (?, ?, ?) ";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setLong(1, reservationWaiting.getReservation().getId());
            preparedStatement.setString(2, reservationWaiting.getName());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(reservationWaiting.getRequestedAt()));
            return preparedStatement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 대기 ID를 생성하지 못했습니다.");
        }

        return reservationWaiting.withId(key.longValue());
    }

    @Override
    public int deleteByIdAndName(Long id, String name) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ? AND name = ?";
        return jdbcTemplate.update(sql, id, name);
    }

    @Override
    public int deleteById(long id) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<ReservationWaiting> findEarliestByReservationId(final long reservationId) {
        String sql = """
                SELECT rw.id AS waiting_id,
                       rw.name AS waiting_name,
                       rw.requested_at,
                       r.id AS reservation_id,
                       r.name AS reservation_name,
                       r.date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url,
                       rt.id AS time_id,
                       rt.start_at
                FROM reservation_waiting AS rw
                INNER JOIN reservation AS r ON rw.reservation_id = r.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                WHERE rw.reservation_id = ?
                ORDER BY rw.requested_at, rw.id
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, waitingRowMapper, reservationId)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByReservationIdAndName(final Long reservationId, final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_waiting WHERE reservation_id = ? AND name = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId, name));
    }

    @Override
    public boolean existsByReservationId(final Long reservationId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_waiting WHERE reservation_id = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId));
    }
}
