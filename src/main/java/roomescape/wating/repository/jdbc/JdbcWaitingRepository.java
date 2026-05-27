package roomescape.wating.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.WaitingRepository;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final static RowMapper<Waiting> WAITING_ROW_MAPPER = ((rs, rowNum) ->
            Waiting.of(
                    rs.getLong("id"),
                    rs.getString("customer_name"),
                    rs.getDate("reservation_date"),
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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Waiting waiting) {
        final String sql = """
                INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waiting.getCustomerName().name());
            ps.setDate(2, Date.valueOf(waiting.getReservationDate()));
            ps.setLong(3, waiting.getTime().getId());
            ps.setLong(4, waiting.getTheme().getId());
            return ps;
        }, keyHolder);

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
    public List<Waiting> findAllByCustomerNameAndReservationDateTimeAfter(
            final String customerName,
            final LocalDateTime now
    ) {
        final String sql = """
                SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                       t.id AS t_id, t.start_at AS t_time,
                       th.id AS th_id, th.name AS th_name, th.description AS th_description, th.thumbnail_url AS th_thumbnail_url
                FROM waiting w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.customer_name = ?
                  AND (w.reservation_date > ? OR (w.reservation_date = ? AND t.start_at > ?))
                ORDER BY w.reservation_date ASC
                """;

        return jdbcTemplate.query(sql,
                WAITING_ROW_MAPPER,
                customerName,
                now,
                Date.valueOf(now.toLocalDate()),
                Time.valueOf(now.toLocalTime()))
        ;
    }
}
