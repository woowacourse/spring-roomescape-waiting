package roomescape.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.domain.ReservationTime;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            ReservationTime.create(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );

    public ReservationTimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return ReservationTime.create(id, reservationTime.getStartAt());
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationTime> findById(Long id) {
        String sql = "select * from reservation_time where id = ?";

        ReservationTime reservationTime;
        try {
            reservationTime = jdbcTemplate.queryForObject(sql, reservationTimeRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return Optional.of(Objects.requireNonNull(reservationTime));
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "SELECT 1 FROM reservation_time WHERE start_at = ? LIMIT 1";
        List<Integer> results = jdbcTemplate.queryForList(sql, Integer.class, startAt);
        return !results.isEmpty();
    }

    @Override
    public List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    r.id is NOT NULL as is_reserved
                FROM
                    reservation_time rt
                LEFT JOIN
                    reservation r ON r.time_id = rt.id AND r.date = ? AND r.theme_id = ? 
                GROUP BY
                    rt.id
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new AvailableReservationTimeResponse(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at")),
                        resultSet.getBoolean("is_reserved")
                ), date, themeId);
    }
}
