package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_MAPPER = (resultSet, row) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(reservationTime);
        Number id = jdbcInsert.executeAndReturnKey(parameterSource);
        return new ReservationTime(id.longValue(), reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAllReservationTimes() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, RESERVATION_TIME_MAPPER);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RESERVATION_TIME_MAPPER, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";

        try {
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException exception) {
            throw new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME);
        }
    }
}
