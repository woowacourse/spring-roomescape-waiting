package roomescape.time.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTimeDao implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(reservationTime);
        Long id = simpleJdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        try {
            String sql = "SELECT * FROM reservation_time WHERE id = ?";
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(reservationTime);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsByStartAt(LocalTime time) {
        String sql = "SELECT EXISTS (SELECT id FROM reservation_time WHERE start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, time);
    }
}
