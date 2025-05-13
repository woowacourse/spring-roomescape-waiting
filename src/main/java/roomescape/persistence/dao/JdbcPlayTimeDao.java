package roomescape.persistence.dao;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.domain.PlayTime;

@Repository
public class JdbcPlayTimeDao implements PlayTimeDao {

    private static final String ID = "id";
    private static final String START_AT = "start_at";
    private static final RowMapper<PlayTime> playTimeRowMapper =
            (rs, rowNum) -> new PlayTime(
                    rs.getLong(ID),
                    LocalTime.parse(rs.getString(START_AT))
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcPlayTimeDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns(ID);
    }

    @Override
    public PlayTime insert(final PlayTime playTime) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(START_AT, playTime.getStartAt());
        final Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new PlayTime(id, playTime.getStartAt());
    }

    @Override
    public List<PlayTime> findAll() {
        final String sql = """
                SELECT id, start_at 
                FROM reservation_time
                """;
        return jdbcTemplate.query(sql, playTimeRowMapper);
    }

    @Override
    public Optional<PlayTime> findById(final Long id) {
        final String sql = """
                SELECT id, start_at 
                FROM reservation_time 
                WHERE id = ?
                """;
        try {
            final PlayTime playTime = jdbcTemplate.queryForObject(sql, playTimeRowMapper, id);
            return Optional.of(playTime);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(final Long id) {
        final String sql = """
                DELETE FROM reservation_time 
                WHERE id = ?
                """;
        final int updatedRowCount = jdbcTemplate.update(sql, id);
        return updatedRowCount >= 1;
    }

    @Override
    public boolean existsById(final Long timeId) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1 
                    FROM reservation_time 
                    WHERE id = ?    
                ) as is_exist
                """;
        final int flag = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return flag == 1;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1 
                    FROM reservation_time 
                    WHERE start_at = ?    
                ) as is_exist
                """;
        final int flag = jdbcTemplate.queryForObject(sql, Integer.class, startAt.toString());
        return flag == 1;
    }
}
