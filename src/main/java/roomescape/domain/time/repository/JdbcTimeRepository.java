package roomescape.domain.time.repository;

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
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.error.type.TimeErrorType;
import roomescape.global.error.exception.GeneralException;

public class JdbcTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcTimeRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation_time")
            .usingColumns("start_at")
            .usingGeneratedKeyColumns("id");
    }


    public Time save(Time time) {
        Map<String, Object> args = Map.of("start_at", time.getStartAt());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return Time.reconstruct(generatedKey, time.getStartAt(), null);
    }


    public List<Time> findAllByDeletedAtIsNull() {
        String sql = "SELECT id, start_at FROM reservation_time WHERE deleted_at IS NULL";
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> Time.reconstruct(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime(),
                null
            )
        );
    }


    public Optional<Time> findTimeByIdAndDeletedAtIsNull(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = :id AND deleted_at IS NULL";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Time time = jdbcTemplate.queryForObject(
                sql,
                parameters,
                (resultSet, rowNum) -> Time.reconstruct(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    null
                )
            );
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public boolean existsTimeByIdAndDeletedAtIsNull(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation_time
                WHERE id = :id
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public boolean existsTimeByStartAtAndDeletedAtIsNull(LocalTime startAt) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation_time
                WHERE start_at = :startAt
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("startAt", startAt);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public void deleteTimeById(Long id) {
        final String sql = """
            UPDATE reservation_time
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE id = :id
              AND deleted_at IS NULL
            """;
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(TimeErrorType.TIME_NOT_FOUND);
        }
    }
}
