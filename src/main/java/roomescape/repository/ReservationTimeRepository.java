package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import roomescape.exception.ErrorCode;
import roomescape.exception.KeyGenerationException;

@Repository
@RequiredArgsConstructor
public class ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ReservationTime> findAll() {
        final String sql = """
                SELECT id, start_at, end_at
                FROM reservation_time
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, this::mapToDomain)
                .stream()
                .toList();
    }

    public Optional<ReservationTime> findById(final Long timeId) {
        final String sql = """
                SELECT id, start_at, end_at
                FROM reservation_time
                WHERE id = ?
                """;

        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    timeId
            );

            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ReservationTime save(final ReservationTime newReservationTime) {
        final long newTimeId = insertReservationTime(newReservationTime);

        return newReservationTime.withId(newTimeId);
    }

    public boolean delete(final Long timeId) {
        final String sql = """
                DELETE FROM reservation_time
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, timeId) > 0;
    }


    private long insertReservationTime(final ReservationTime reservationTime) {
        final String sql = """
                INSERT INTO reservation_time (start_at, end_at)
                VALUES (?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            preparedStatement.setTime(2, Time.valueOf(reservationTime.getEndAt()));

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private static long generatedIdFrom(final KeyHolder keyHolder) {
        final Number generatedKey = keyHolder.getKey();

        if (generatedKey == null) {
            throw new KeyGenerationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return generatedKey.longValue();
    }

    /**
     * ResultSet - Domain 매핑 메서드
     */
    private ReservationTime mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return ReservationTime.createWithId(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime(),
                resultSet.getTime("end_at").toLocalTime()
        );
    }
}
