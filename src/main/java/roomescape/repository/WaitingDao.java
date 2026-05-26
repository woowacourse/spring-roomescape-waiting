package roomescape.repository;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Waiting;

@Repository
public class WaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Waiting> waitingRowMapper = (rs, rowNum) -> new Waiting(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getLong("reservation_id")
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

    public Waiting findById(long id) {
        String sql = """
                SELECT  w.id AS wait_id,
                        w.name AS name,
                        w.reservation_id AS reservation_id
                FROM waiting AS w
                where w.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, waitingRowMapper, id);
    }

    public int findOrderByReservationId(long id, long reservationId) {
        String sql = """
                SELECT COUNT(*)
                FROM waiting w
                WHERE w.reservation_id = ?
                  AND w.id <= ?
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservationId,
                id
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }
}
