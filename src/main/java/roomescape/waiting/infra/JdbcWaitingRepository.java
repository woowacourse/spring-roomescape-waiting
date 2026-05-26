package roomescape.waiting.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
}
