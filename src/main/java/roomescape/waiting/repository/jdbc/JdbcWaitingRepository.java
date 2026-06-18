package roomescape.waiting.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.repository.dto.WaitingWithRank;

@Profile("jdbc")
@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final static RowMapper<Waiting> WAITING_ROW_MAPPER = ((rs, rowNum) ->
        Waiting.of(
            rs.getLong("id"),
            rs.getString("customer_name"),
            rs.getDate("reservation_date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            ReservationTime.of(
                rs.getLong("t_id"),
                rs.getTime("t_time").toLocalTime()
            ),
            Theme.of(
                rs.getLong("th_id"),
                rs.getString("th_name"),
                rs.getString("th_description"),
                rs.getString("th_thumbnail_url")
            )
        ));

    private final static RowMapper<WaitingWithRank> WAITING_WITH_RANK_ROW_MAPPER = ((rs, rowNum) ->
        new WaitingWithRank(
            Waiting.of(
                rs.getLong("id"),
                rs.getString("customer_name"),
                rs.getDate("reservation_date").toLocalDate(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                ReservationTime.of(
                    rs.getLong("t_id"),
                    rs.getTime("t_time").toLocalTime()
                ),
                Theme.of(
                    rs.getLong("th_id"),
                    rs.getString("th_name"),
                    rs.getString("th_description"),
                    rs.getString("th_thumbnail_url")
                )
            ),
            rs.getInt("rank")
        ));

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Waiting save(final Waiting waiting) {
        final String sql = """
            INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waiting.getCustomerNameValue());
            ps.setDate(2, Date.valueOf(waiting.getReservationDate()));
            ps.setLong(3, waiting.getTimeId());
            ps.setLong(4, waiting.getThemeId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("insert 성공 후 generated key를 가져올 수 없습니다.");
        }
        final long id = key.longValue();
        return findById(id)
            .orElseThrow(() -> new IllegalStateException("insert 성공 후 waiting 조회에 실패했습니다"));
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
            SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                   t.id AS t_id, t.start_at AS t_time,
                   th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
            FROM waiting w
            JOIN reservation_time t ON w.time_id = t.id
            JOIN theme th ON w.theme_id = th.id
            WHERE w.id = ?
            """;
        return jdbcTemplate.query(sql, WAITING_ROW_MAPPER, id).stream()
            .findFirst();
    }

    @Override
    public Optional<Waiting> findEarliestBySlotForUpdate(final LocalDate date, final long timeId, final long themeId) {
        final String sql = """
            SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                   t.id AS t_id, t.start_at AS t_time,
                   th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
            FROM waiting w
            JOIN reservation_time t ON w.time_id = t.id
            JOIN theme th ON w.theme_id = th.id
            WHERE w.reservation_date = ? AND w.time_id = ? AND w.theme_id = ?
            ORDER BY w.created_at ASC
            LIMIT 1
            FOR UPDATE
            """;
        return jdbcTemplate.query(sql, WAITING_ROW_MAPPER, Date.valueOf(date), timeId, themeId)
            .stream()
            .findFirst();
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByCustomerNameAndReservationDateTimeAfter(
        final String customerName,
        final LocalDateTime now
    ) {
        final String sql = """
            WITH ranked AS (
                 SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                        w.time_id, w.theme_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY w.reservation_date, w.time_id, w.theme_id
                            ORDER BY w.created_at
                        ) AS rank
                 FROM waiting w
            )
            SELECT r.id, r.customer_name, r.reservation_date, r.created_at, r.rank,
                   t.id AS t_id, t.start_at AS t_time,
                   th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
            FROM ranked r
            JOIN reservation_time t ON r.time_id = t.id
            JOIN theme th ON r.theme_id = th.id
            WHERE r.customer_name = ?
              AND (r.reservation_date > ? OR (r.reservation_date = ? AND t.start_at > ?))
            ORDER BY r.reservation_date ASC
            """;

        return jdbcTemplate.query(
            sql,
            WAITING_WITH_RANK_ROW_MAPPER,
            customerName,
            now,
            Date.valueOf(now.toLocalDate()),
            Time.valueOf(now.toLocalTime())
        );
    }

    @Override
    public List<WaitingWithRank> findAllWithRank() {
        final String sql = """
            WITH ranked AS (
                 SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                        w.time_id, w.theme_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY w.reservation_date, w.time_id, w.theme_id
                            ORDER BY w.created_at
                        ) AS rank
                 FROM waiting w
            )
            SELECT r.id, r.customer_name, r.reservation_date, r.created_at, r.rank,
                   t.id AS t_id, t.start_at AS t_time,
                   th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
            FROM ranked r
            JOIN reservation_time t ON r.time_id = t.id
            JOIN theme th ON r.theme_id = th.id
            ORDER BY r.reservation_date ASC, t.start_at ASC, r.rank ASC
            """;
        return jdbcTemplate.query(sql, WAITING_WITH_RANK_ROW_MAPPER);
    }

    @Override
    public boolean existsBySlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        final String sql = """
            SELECT COUNT(1)
            FROM waiting
            WHERE reservation_date = ? AND time_id = ? AND theme_id = ?
            """;
        final Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            Date.valueOf(reservationDate),
            timeId,
            themeId
        );

        return count != null && count != 0;
    }
}
