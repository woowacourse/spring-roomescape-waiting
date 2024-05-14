package roomescape.reservation.dao;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

@Repository
public class ReservationTimeDao implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<ReservationTime> rowMapper = (ResultSet resultSet, int rowNum) -> {
        return new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        );
    };

    private final ResultSetExtractor<Optional<ReservationTime>> optionalResultSetExtractor = (ResultSet resultSet) -> {
        if (resultSet.next()) {
            ReservationTime reservationTime = new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );
            return Optional.of(reservationTime);
        } else {
            return Optional.empty();
        }
    };

    public ReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(reservationTime);
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time;";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(long timeId) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?;";
        return jdbcTemplate.query(sql, optionalResultSetExtractor, timeId);
    }

    @Override
    public void deleteById(long timeId) {
        String sql = "DELETE FROM reservation_time WHERE id = ?;";
        jdbcTemplate.update(sql, timeId);
    }

    @Override
    public boolean existsByStartAt(LocalTime time) {
        String sql = "SELECT * FROM reservation_time WHERE start_at = ?;";
        List<ReservationTime> times = jdbcTemplate.query(sql, rowMapper, time);
        return !times.isEmpty();
    }

    @Override
    public Set<ReservationTime> findReservedTime(LocalDate date, long themeId) {
        String sql = """
                SELECT * FROM reservation_time 
                INNER JOIN reservation as re 
                ON re.time_id = reservation_time.id 
                INNER JOIN member_reservation as mr 
                ON re.id = mr.reservation_id 
                WHERE re.date = ? AND re.theme_id = ?;
                """;

        return new HashSet<>(jdbcTemplate.query(sql, rowMapper, date, themeId));
    }
}
