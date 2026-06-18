package roomescape.reservationtime.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationTime> findAll() {
        final String sql = """
                SELECT id, start_at
                FROM reservation_time
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, this::mapToDomain)
                .stream()
                .toList();
    }

    @Override
    public Optional<ReservationTime> findById(final Long timeId) {
        final String sql = """
                SELECT id, start_at
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

    @Override
    public ReservationTime save(final ReservationTime newReservationTime) {
        final long newTimeId = insertReservationTime(newReservationTime);

        return ReservationTime.of(
                newTimeId,
                newReservationTime.getStartAt()
        );
    }

    @Override
    public boolean delete(final Long timeId) {
        final String sql = """
                DELETE FROM reservation_time
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, timeId) > 0;
    }

    private long insertReservationTime(final ReservationTime reservationTime) {
        final String sql = """
                INSERT INTO reservation_time (start_at)
                VALUES (?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setTime(1, Time.valueOf(reservationTime.getStartAt()));

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성된 id를 가져오지 못했습니다.");
        }

        return keyHolder.getKey().longValue();
    }

    private ReservationTime mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return ReservationTime.of(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        );
    }

}
