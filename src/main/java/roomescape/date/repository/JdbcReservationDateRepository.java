package roomescape.date.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationDateRepository implements ReservationDateRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<ReservationDate> reservationDateRowMapper = (resultSet, rowMapper) ->
            ReservationDate.load(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getBoolean("is_active")
            );

    public JdbcReservationDateRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation_date")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<ReservationDate> findById(Long id) {
        String sql = "SELECT id, date, is_active FROM reservation_date where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, reservationDateRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationDate> findAll() {
        String sql = "SELECT id, date, is_active FROM reservation_date";
        return jdbcTemplate.query(sql, reservationDateRowMapper);
    }

    @Override
    public List<ReservationDate> findAllAfterToday() {
        String sql = """
                SELECT id, date, is_active
                FROM reservation_date 
                WHERE date >= :today
                AND is_active = true
                ORDER BY date ASC
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", LocalDate.now());

        return jdbcTemplate.query(sql, params, reservationDateRowMapper);
    }

    @Override
    public ReservationDate save(ReservationDate reservationDate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservationDate.getDate())
                .addValue("is_active", reservationDate.isActive());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return ReservationDate.load(id, reservationDate.getDate(), reservationDate.isActive());
    }

    @Override
    public boolean updateStatus(ReservationDate reservationDate) {
        String sql = """
                UPDATE reservation_date 
                SET is_active = :is_active
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservationDate.getId())
                .addValue("is_active", reservationDate.isActive());

        int updateCount = jdbcTemplate.update(sql, params);
        return updateCount > 0;
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM reservation_date WHERE date = :date";
        MapSqlParameterSource params = new MapSqlParameterSource("date", date);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

}
