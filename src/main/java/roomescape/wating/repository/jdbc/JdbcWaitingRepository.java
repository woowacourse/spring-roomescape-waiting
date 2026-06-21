package roomescape.wating.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.entity.WaitingEntity;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.repository.dto.WaitingWithRank;

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
                        rs.getString("th_thumbnail_url"),
                        rs.getInt("th_price")
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
                        rs.getString("customer_email"),
                        slot,
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
    );

    private static final RowMapper<WaitingWithRank> WAITING_WITH_RANK_ROW_MAPPER = ((rs, rowNum) ->
            new WaitingWithRank(
                    WAITING_ROW_MAPPER.mapRow(rs, rowNum),
                    rs.getInt("waiting_rank")
            )
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Waiting waiting) {
        final WaitingEntity waitingEntity = toEntity(waiting);

        final String sql = """
                INSERT INTO waiting(customer_name, customer_email, slot_id)
                SELECT ?, ?, ?
                FROM reservation
                WHERE slot_id = ?
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updateCount = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waitingEntity.customerName());
            ps.setString(2, waitingEntity.customerEmail());
            ps.setLong(3, waitingEntity.slotId());
            ps.setLong(4, waitingEntity.slotId());
            return ps;
        }, keyHolder);

        if (updateCount == 0) {
            throw new UnprocessableContentException("예약이 존재하지 않는 슬롯에는 대기를 신청할 수 없습니다.");
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
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
                SELECT w.id, w.customer_name, w.customer_email, w.created_at,
                       s.id AS slot_id, s.reservation_date,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description,
                       th.thumbnail_url AS th_thumbnail_url, th.price AS th_price
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE w.id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    WAITING_ROW_MAPPER,
                    id
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Waiting> findEarliestBySlotId(final Long slotId) {
        final String sql = """
                SELECT w.id, w.customer_name, w.customer_email, w.created_at,
                       s.id AS slot_id, s.reservation_date,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description,
                       th.thumbnail_url AS th_thumbnail_url, th.price AS th_price
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE w.slot_id = ?
                ORDER BY w.created_at ASC, w.id ASC
                LIMIT 1
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    WAITING_ROW_MAPPER,
                    slotId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
            final String customerName,
            final String customerEmail,
            final LocalDateTime now
    ) {
        final String sql = """
                WITH ranked_waiting AS (
                    SELECT
                        w.id,
                        w.customer_name,
                        w.customer_email,
                        w.created_at,
                        w.slot_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY w.slot_id
                            ORDER BY w.created_at ASC, w.id ASC
                        ) AS waiting_rank
                    FROM waiting w
                )
                SELECT
                    rw.id,
                    rw.customer_name,
                    rw.customer_email,
                    rw.created_at,
                    s.id AS slot_id,
                    s.reservation_date,
                    t.id AS t_id,
                    t.start_at AS t_time,
                    th.id AS th_id,
                    th.name AS th_name,
                    th.description AS th_description,
                    th.thumbnail_url AS th_thumbnail_url,
                    th.price AS th_price,
                    rw.waiting_rank
                FROM ranked_waiting rw
                JOIN reservation_slot s ON rw.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme th ON s.theme_id = th.id
                WHERE rw.customer_name = ?
                  AND rw.customer_email = ?
                  AND (s.reservation_date > ? OR (s.reservation_date = ? AND t.start_at > ?))
                ORDER BY s.reservation_date ASC, t.start_at ASC
                """;

        return jdbcTemplate.query(sql,
                WAITING_WITH_RANK_ROW_MAPPER,
                customerName,
                customerEmail,
                Date.valueOf(now.toLocalDate()),
                Date.valueOf(now.toLocalDate()),
                Time.valueOf(now.toLocalTime())
        );
    }

    private WaitingEntity toEntity(final Waiting waiting) {
        return new WaitingEntity(
                waiting.getId(),
                waiting.getCustomerName().name(),
                waiting.getCustomerEmail(),
                waiting.getSlotId()
        );
    }
}
