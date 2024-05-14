package roomescape.reservationtime.repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.model.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<ReservationTime> reservationTimeRowMapper =
            (resultSet, rowNum) -> new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    public ReservationTime save(final ReservationTime reservationTime) {
        SqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("start_at", Time.valueOf(reservationTime.getTime()));
        long id = simpleJdbcInsert.executeAndReturnKey(mapSqlParameterSource).longValue();
        return new ReservationTime(id, reservationTime.getTime());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(final Long timeId) {
        String sql = """
                select id, start_at 
                from reservation_time 
                where id = ?
                limit 1
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reservationTimeRowMapper, timeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(final Long id) {
        String sql = """
                select exists ( select 1
                from reservation_time as r
                where r.id = ? ) 
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public boolean existsByStartAt(final LocalTime time) {
        String sql = """
                select exists ( select 1
                from reservation_time as r
                where r.start_at = ? )
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, time) != 0;
    }

    @Override
    public void deleteById(final Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
