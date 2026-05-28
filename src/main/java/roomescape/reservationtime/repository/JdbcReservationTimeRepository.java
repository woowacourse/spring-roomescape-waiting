package roomescape.reservationtime.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {


    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        insert(reservationTime, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return reservationTime.withId(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("""
                SELECT id, start_at, deleted_at
                FROM reservation_time
                WHERE deleted_at IS NULL
                """, reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, start_at, deleted_at
                        FROM reservation_time
                        WHERE id = ? AND deleted_at IS NULL
                        """, reservationTimeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation_time
                WHERE start_at = ? AND deleted_at IS NULL
                """, Integer.class, startAt.toString());
        return count != null && count > 0;
    }

    @Override
    public boolean cancelById(Long id, LocalDateTime now) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation_time
                SET deleted_at = ?, delete_token = ?
                WHERE id = ? AND deleted_at IS NULL
                """, now, id, id);
        return rowCount > 0;
    }

    private void insert(ReservationTime reservationTime, KeyHolder keyHolder) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation_time (start_at)
                            VALUES (?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservationTime.getStartAt().toString());
            return preparedStatement;
        }, keyHolder);
    }

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            ReservationTime.of(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at")),
                    toLocalDateTime(resultSet.getTimestamp("deleted_at"))
            );

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
