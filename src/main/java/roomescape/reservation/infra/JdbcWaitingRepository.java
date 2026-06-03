package roomescape.reservation.infra;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.Rank;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<ReservationSlot> findSlotById(Long id) {
        return jdbcTemplate.query("""
                        SELECT w.date, w.theme_id, w.time_id, rt.start_at
                            FROM waiting w
                            JOIN reservation_time rt ON w.time_id = rt.id
                            WHERE w.id = ?
                        """,
                (rs, rowNum) -> ReservationSlot.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .themeId(rs.getLong("theme_id"))
                        .timeId(rs.getLong("time_id"))
                        .startAt(rs.getObject("start_at", LocalTime.class))
                        .build()
                , id).stream().findFirst();
    }

    @Override
    public Optional<Waiting> findFirstBySlot(ReservationSlot slot) {
        return jdbcTemplate.query("""
                                SELECT w.id, w.name, w.rank
                                FROM waiting w
                                WHERE w.date = ?
                                  AND w.theme_id = ?
                                  AND w.time_id = ?
                                ORDER BY w.rank ASC
                                LIMIT 1
                                """,
                        (rs, rowNum) -> toWaiting(rs.getLong("id"), rs.getString("name"), rs.getInt("rank"), slot),
                        slot.date(),
                        slot.themeId(),
                        slot.timeId())
                .stream().findFirst();
    }

    @Override
    public Waiting save(Waiting waiting) {
        ReservationSlot slot = waiting.getSlot();
        Rank rank = getNextRank(slot);
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getUserName())
                .addValue("date", slot.date())
                .addValue("theme_id", slot.themeId())
                .addValue("time_id", slot.timeId())
                .addValue("rank", rank.value());

        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return waiting.withId(id).withRank(rank);
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    private Rank getNextRank(ReservationSlot slot) {
        Integer rank = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(MAX(rank), 0) + 1
                        FROM waiting
                        WHERE date = ?
                          AND theme_id = ?
                          AND time_id = ?
                        """,
                Integer.class,
                slot.date(),
                slot.themeId(),
                slot.timeId());

        return Rank.builder()
                .value(rank)
                .build();
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    private Waiting toWaiting(Long id, String name, int rankValue, ReservationSlot slot) {
        User user = User.builder()
                .name(name)
                .build();
        Rank rank = Rank.builder()
                .value(rankValue)
                .build();

        return Waiting.builder()
                .id(id)
                .user(user)
                .slot(slot)
                .rank(rank)
                .build();
    }
}
