package roomescape.time.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public class TimeDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> rowMapper;

    public TimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = (resultSet, rowNum) -> new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getObject("start_at", LocalTime.class)
        );
    }

    public List<ReservationTime> findTimes() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<ReservationTime> findTimeById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<ReservationTime> findTimesExistsReservationDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT id, start_at 
                FROM reservation_time
                WHERE id IN (
                    SELECT time_id
                    FROM reservation
                    WHERE date = ? AND theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, rowMapper, date, themeId);
    }

    public ReservationTime createTime(ReservationTime time) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> createPreparedStatementForUpdate(connection, time), keyHolder);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("해당 시간은 이미 존재합니다.");
        }

        Long id = keyHolder.getKey().longValue();
        return time.withId(id);
    }

    private PreparedStatement createPreparedStatementForUpdate(Connection connection, ReservationTime time)
            throws SQLException {
        String sql = "INSERT INTO reservation_time(start_at) VALUES (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
        preparedStatement.setObject(1, time.getStartAt());
        return preparedStatement;
    }

    public void deleteTime(Long id) {
        try {
            String sql = "DELETE FROM reservation_time WHERE id = ?";
            jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("해당 시간대는 이미 예약되어 있어 삭제할 수 없습니다.", exception);
        }
    }
}
