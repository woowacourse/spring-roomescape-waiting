package roomescape.wating.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.NoReservationForWaitingException;
import roomescape.wating.repository.entity.WaitingEntity;
import roomescape.wating.repository.WaitingRepository;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private static final RowMapper<Waiting> WAITING_ROW_MAPPER = ((rs, rowNum) ->
            {
                final ReservationTime reservationTime = ReservationTime.of(
                        rs.getLong("t_id"),
                        rs.getTime("t_time").toLocalTime()
                );
                final Theme theme = Theme.of(
                        rs.getLong("th_id"),
                        rs.getString("th_name"),
                        rs.getString("th_description"),
                        rs.getString("th_thumbnail_url")
                );
                final ReservationSlot slot = ReservationSlot.of(
                        rs.getLong("slot_id"),
                        rs.getDate("reservation_date").toLocalDate(),
                        reservationTime,
                        theme
                );

                return Waiting.of(
                        rs.getLong("id"),
                        rs.getString("customer_name"),
                        slot,
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Waiting waiting) {
        final WaitingEntity waitingEntity = toEntity(waiting);

        final String sql = """
                INSERT INTO waiting(customer_name, slot_id)
                SELECT ?, ?
                FROM reservation
                WHERE slot_id = ?
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updateCount = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waitingEntity.customerName());
            ps.setLong(2, waitingEntity.slotId());
            ps.setLong(3, waitingEntity.slotId());
            return ps;
        }, keyHolder);

        if (updateCount == 0) {
            throw new NoReservationForWaitingException();
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
    }

    private WaitingEntity toEntity(final Waiting waiting) {
        return new WaitingEntity(
                waiting.getId(),
                waiting.getCustomerName().name(),
                waiting.getSlotId()
        );
    }

    @Override
    public boolean deleteById(final long id) {
        final String sql = """
                DELETE FROM waiting
                WHERE id = ?
                """;

        final int updateCount = jdbcTemplate.update(sql, id);
        return updateCount > 0;
    }

    @Override
    public Optional<Waiting> findById(final long id) {
        final String sql = """
                SELECT w.id, w.customer_name, w.created_at,
                       s.id AS slot_id, s.reservation_date,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE w.id = ?
                """;
        return jdbcTemplate.query(sql, WAITING_ROW_MAPPER, id).stream()
                .findFirst();
    }

    @Override
    public Optional<Waiting> findEarliestBySlotId(final Long slotId) {
        final String sql = """
                SELECT w.id, w.customer_name, w.created_at,
                       s.id AS slot_id, s.reservation_date,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE w.slot_id = ?
                ORDER BY w.created_at ASC, w.id ASC
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, WAITING_ROW_MAPPER, slotId)
                .stream()
                .findFirst();
    }

    @Override
    public int countEarlierWaitingsInSlot(
            final Long slotId,
            final LocalDateTime createdAt,
            final long waitingId
    ) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting
                WHERE slot_id = ?
                  AND (created_at < ? OR (created_at = ? AND id < ?))
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slotId,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt),
                waitingId
        );
        return count == null ? 0 : count;
    }

    @Override
    public List<Waiting> findAllByCustomerNameAndReservationDateTimeAfter(
            final String customerName,
            final LocalDateTime now
    ) {
        final String sql = """
                SELECT w.id, w.customer_name, w.created_at,
                       s.id AS slot_id, s.reservation_date,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE w.customer_name = ?
                  AND (s.reservation_date > ? OR (s.reservation_date = ? AND t.start_at > ?))
                ORDER BY s.reservation_date ASC
                """;

        return jdbcTemplate.query(sql,
                WAITING_ROW_MAPPER,
                customerName,
                Date.valueOf(now.toLocalDate()),
                Date.valueOf(now.toLocalDate()),
                Time.valueOf(now.toLocalTime())
        );
    }
}
