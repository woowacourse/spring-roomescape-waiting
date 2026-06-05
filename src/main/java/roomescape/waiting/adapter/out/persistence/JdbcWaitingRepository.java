package roomescape.waiting.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.waiting.application.port.out.WaitingRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Waiting> waitingRowMapper = (resultSet, rowNum) -> Waiting.of(
            resultSet.getLong("id"),
            resultSet.getLong("member_id"),
            resultSet.getLong("slot_id")
    );

    private final RowMapper<WaitingDetailProjection> waitingDetailRowMapper = (resultSet, rowNum) ->
            new WaitingDetailProjection(
                    resultSet.getLong("waiting_id"),
                    resultSet.getLong("slot_id"),
                    resultSet.getString("member_name"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail_url"),
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    @Override
    public Waiting save(Waiting waiting) {
        String sql = "INSERT INTO waiting(member_id, slot_id) VALUES (:memberId, :slotId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", waiting.getMemberId())
                .addValue("slotId", waiting.getSlotId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("waiting 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return Waiting.of(id.longValue(), waiting.getMemberId(), waiting.getSlotId());
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        String sql = """
                SELECT id, member_id, slot_id
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
    public Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        String sql = """
                SELECT s.time_id
                FROM waiting w
                JOIN slot s ON w.slot_id = s.id
                WHERE s.date = :date
                AND s.theme_id = :themeId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return Set.copyOf(jdbcTemplate.query(sql, params, (resultSet, rowNum) -> resultSet.getLong("time_id")));
    }

    @Override
    public boolean existsBySlotIdAndMemberId(long memberId, long slotId) {
        String sql = """
                SELECT EXISTS (SELECT 1 FROM waiting WHERE member_id = :memberId AND slot_id = :slotId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("slotId", slotId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public boolean existsBySlotId(long slotId) {
        String sql = """
                SELECT EXISTS (SELECT 1 FROM waiting WHERE slot_id = :slotId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public List<Waiting> findAllBySlotIdOrderById(long slotId) {
        String sql = """
                SELECT id, member_id, slot_id
                FROM waiting
                WHERE slot_id = :slotId
                ORDER BY id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        return jdbcTemplate.query(sql, params, waitingRowMapper);
    }

    @Override
    public List<Waiting> findAllBySlotIds(List<Long> slotIds) {
        if (slotIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT id, member_id, slot_id
                FROM waiting
                WHERE slot_id IN (:slotIds)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotIds", slotIds);

        return jdbcTemplate.query(sql, params, waitingRowMapper);
    }

    @Override
    public List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId) {
        String sql = """
                SELECT
                    w.id AS waiting_id,
                    w.slot_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    rt.id AS time_id,
                    rt.start_at
                FROM waiting w
                JOIN slot s ON w.slot_id = s.id
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
