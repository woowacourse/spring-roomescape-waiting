package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> TIME_ROW_MAPPER = (selectedTime, rowNum) ->
            new ReservationTime(selectedTime.getLong("id"), LocalTime.parse(selectedTime.getString("start_at")));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationTimeInsert;

    public ReservationTimeJdbcRepository( JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(reservationTime);
        Long savedTimeId = reservationTimeInsert.executeAndReturnKey(parameterSource).longValue();
        return new ReservationTime(savedTimeId, reservationTime.getStartAt());
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String selectQuery = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            ReservationTime time = jdbcTemplate.queryForObject(selectQuery, TIME_ROW_MAPPER, id);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        String selectQuery = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(selectQuery, TIME_ROW_MAPPER);
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = """
                SELECT
                CASE WHEN EXISTS (
                        SELECT 1
                        FROM reservation_time
                        WHERE start_at = ?
                    )
                    THEN TRUE
                    ELSE FALSE
                END""";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }
}
