package roomescape.reservation.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReservationTimeDao implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(DataSource source) {
        this.jdbcTemplate = new JdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(reservationTime);
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(Objects.requireNonNull(id), reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, this::mapRowToObject);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        try {
            String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, this::mapRowToObject, id);
            return Optional.ofNullable(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private ReservationTime mapRowToObject(ResultSet resultSet, int rowNumber) throws SQLException {
        LocalTime startAt = resultSet.getTime("start_at")
                .toLocalTime();
        ReservationTime reservationTime = new ReservationTime(startAt);
        return new ReservationTime(resultSet.getLong("id"), reservationTime);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
