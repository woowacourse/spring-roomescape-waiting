package roomescape.infrastructure.persistance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, DataSource dataSource) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long create(ReservationTime reservationTime) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(reservationTime);
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }

    @Override
    public Optional<ReservationTime> findById(Long reservationTimeId) {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                WHERE id = :id
                """;
        try {
            ReservationTime reservationTime = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Map.of("id", reservationTimeId),
                    RESERVATION_TIME_ROW_MAPPER
            );
            return Optional.ofNullable(reservationTime);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                """;
        return namedParameterJdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }

    @Override
    public void deleteById(Long reservationTimeId) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = :id
                """;
        namedParameterJdbcTemplate.update(sql, Map.of("id", reservationTimeId));
    }
}
