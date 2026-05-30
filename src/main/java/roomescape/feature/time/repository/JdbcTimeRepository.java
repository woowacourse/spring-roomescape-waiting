package roomescape.feature.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.domain.TimeStatus;
import roomescape.feature.time.error.type.TimeErrorType;
import roomescape.global.error.exception.GeneralException;

@Repository
public class JdbcTimeRepository implements TimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcTimeRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation_time")
            .usingColumns("start_at")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Time save(Time time) {
        Map<String, Object> args = Map.of("start_at", time.getStartAt());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return Time.reconstruct(generatedKey, time.getStartAt(), TimeStatus.ACTIVE);
    }

    @Override
    public List<Time> findAllByNotDeleted() {
        String sql = "SELECT id, start_at, status FROM reservation_time WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> Time.reconstruct(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime(),
                TimeStatus.valueOf(rs.getString("status"))
            )
        );
    }

    @Override
    public Optional<Time> findTimeByIdAndNotDeleted(Long id) {
        String sql = "SELECT id, start_at, status FROM reservation_time WHERE id = :id AND status = 'ACTIVE'";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Time time = jdbcTemplate.queryForObject(
                sql,
                parameters,
                (resultSet, rowNum) -> Time.reconstruct(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    TimeStatus.valueOf(resultSet.getString("status"))
                )
            );
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsTimeByIdAndNotDeleted(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation_time
                WHERE id = :id
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsTimeByStartAtAndNotDeleted(LocalTime startAt) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation_time
                WHERE start_at = :startAt
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("startAt", startAt);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void deleteTimeById(Long id) {
        final String sql = """
            UPDATE reservation_time
            SET status = 'DELETED'
            WHERE id = :id
              AND status = 'ACTIVE'
            """;
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(TimeErrorType.TIME_NOT_FOUND);
        }
    }
}
