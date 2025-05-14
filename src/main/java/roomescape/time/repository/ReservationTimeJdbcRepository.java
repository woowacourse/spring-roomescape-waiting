package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.out.ReservationTimeRepository;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationTime save(ReservationTime reservationTime) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return ReservationTime.load(id, reservationTime.getStartAt());
    }

    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time";
        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                ReservationTime.load(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                ));
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationTime> findById(Long id) {
        String sql = "select * from reservation_time where id = ?";

        ReservationTime reservationTime;
        try {
            reservationTime = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) ->
                    ReservationTime.load(
                            resultSet.getLong("id"),
                            LocalTime.parse(resultSet.getString("start_at"))
                    ), id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return Optional.of(Objects.requireNonNull(reservationTime));
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation_time
                    WHERE start_at = ?
                )
                """;
        int count = jdbcTemplate.queryForObject(sql, Integer.class, startAt);
        return count > 0;
    }
}
