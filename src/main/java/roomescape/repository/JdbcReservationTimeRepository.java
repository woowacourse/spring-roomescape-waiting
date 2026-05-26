package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservedTimeResponseDTO;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(reservationTime)
        ).longValue();

        return ReservationTime.of(
                generatedKey,
                reservationTime.getStartAt()
        );
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "select id, start_at from reservation_time where id = :id";

        Map<String, Object> params = Map.of("id", id);

        List<ReservationTime> results = jdbcTemplate.query(
                sql,
                params,
                (resultSet, rowNum) -> ReservationTime.of(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                )
        );
        return results.stream()
                .findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> ReservationTime.of(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                )
        );
    }

    @Override
    public List<ReservedTimeResponseDTO> findReservedTimes(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                rt.id AS reservation_time_id,
                rt.start_at AS start_at,
                r.date AS date,
                r.id AS reservation_id
                FROM reservation_time AS rt
                LEFT JOIN reservation AS r
                ON rt.id = r.time_id
                AND r.date = :date
                AND r.theme_id = :theme_id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("theme_id", themeId);

        return jdbcTemplate.query(
                sql,
                params,
                (resultSet, rowNum) -> ReservedTimeResponseDTO.create(
                        resultSet.getLong("reservation_time_id"),
                        LocalTime.parse(resultSet.getString("start_at")),
                        resultSet.getObject("reservation_id", Long.class)
                )
        );
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from reservation_time where id = :id";
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = "select count(*) from reservation_time where start_at = :start_at";
        Map<String, Object> params = Map.of("start_at", startAt);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count > 0;
    }
}
