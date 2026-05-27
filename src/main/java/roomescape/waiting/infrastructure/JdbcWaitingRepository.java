package roomescape.waiting.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.waiting.Waiting;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Waiting> waitingRowMapper = (resultSet, rowNum) -> new Waiting(
            resultSet.getLong("id"),
            resultSet.getLong("member_id"),
            resultSet.getLong("schedule_id")
    );

    private final RowMapper<WaitingDetailProjection> waitingDetailRowMapper = (resultSet, rowNum) ->
            new WaitingDetailProjection(
                    resultSet.getLong("waiting_id"),
                    resultSet.getString("member_name"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail_url"),
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    resultSet.getLong("waiting_order")
            );

    @Override
    public Waiting save(Waiting waiting) {
        String sql = "INSERT INTO waiting(member_id, schedule_id) VALUES (:memberId, :scheduleId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", waiting.getMemberId())
                .addValue("scheduleId", waiting.getScheduleId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("waiting 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return new Waiting(id.longValue(), waiting.getMemberId(), waiting.getScheduleId());
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        String sql = """
                SELECT id, member_id, schedule_id
                FROM waiting
                WHERE id = :waitingId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("waitingId", waitingId);

        return jdbcTemplate.query(sql, params, waitingRowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByScheduleIdAndMemberId(long memberId, long scheduleId) {
        String sql = """
                SELECT EXISTS (SELECT 1 FROM waiting WHERE member_id = :memberId AND schedule_id = :scheduleId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("scheduleId", scheduleId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public long countByScheduleIdAndIdLessThanEqual(long scheduleId, long waitingId) {
        String sql = """
                SELECT COUNT(*) FROM waiting
                WHERE schedule_id = :scheduleId
                AND id <= :waitingId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("scheduleId", scheduleId)
                .addValue("waitingId", waitingId);

        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @Override
    public List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId) {
        String sql = """
                SELECT
                    w.id AS waiting_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    rt.id AS time_id,
                    rt.start_at,
                    (
                        SELECT COUNT(*)
                        FROM waiting previous_waiting
                        WHERE previous_waiting.schedule_id = w.schedule_id
                        AND previous_waiting.id <= w.id
                    ) AS waiting_order
                FROM waiting w
                JOIN schedule s ON w.schedule_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON w.member_id = m.id
                WHERE m.id = :memberId
                ORDER BY w.id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return jdbcTemplate.query(sql, params, waitingDetailRowMapper);
    }

    @Override
    public void deleteById(long waitingId) {
        String sql = "DELETE FROM waiting WHERE id = :waitingId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("waitingId", waitingId);

        jdbcTemplate.update(sql, params);
    }
}
