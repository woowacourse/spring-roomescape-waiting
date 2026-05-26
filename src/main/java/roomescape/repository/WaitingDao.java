package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;

import java.util.Map;

@Repository
public class WaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Waiting> waitingRowMapper = (rs, rowNum) -> new Waiting(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("waitNumber")
    );

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, Long reservationId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "reservation_id", reservationId
        )).longValue();
    }

    public Waiting findByName(String name) {
        String sql = """
                SELECT  w.id AS wait_id,
                        w.name AS name,
                        w.reservationId AS reservation_id
                FROM waiting AS w
                where w.name = ?
                """;
        return jdbcTemplate.queryForObject(sql, waitingRowMapper, name);
    }
}
