package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithOrder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationWaitingJdbcRepository implements ReservationWaitingRepository {

    private static final String WAITING_COLUMNS = """
            rw.id as waiting_id,
            rw.name as waiting_name,
            rw.created_at,
            r.id as reservation_id,
            r.name as reservation_name,
            r.date,
            r.reservation_status,
            t.id as time_id,
            t.start_at as time_value,
            th.id as theme_id,
            th.name as theme_name,
            th.description as theme_description,
            th.thumbnail_image_url as theme_thumbnail
            """;

    private static final String WAITING_ORDER_COLUMN = """
            , (
                SELECT COUNT(*)
                FROM reservation_waiting as previous_rw
                WHERE previous_rw.reservation_id = rw.reservation_id
                AND (
                    previous_rw.created_at < rw.created_at
                    OR (previous_rw.created_at = rw.created_at AND previous_rw.id <= rw.id)
                )
            ) as waiting_order
            """;

    private static final String FROM_WAITING = """
            FROM reservation_waiting as rw
            INNER JOIN reservation as r ON rw.reservation_id = r.id
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationWaiting> waitingRowMapper = (rs, rowNum) -> mapWaiting(rs);

    private final RowMapper<WaitingWithOrder> waitingWithOrderRowMapper =
            (rs, rowNum) -> new WaitingWithOrder(mapWaiting(rs), rs.getInt("waiting_order"));

    public ReservationWaitingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private ReservationWaiting mapWaiting(ResultSet rs) throws SQLException {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        Reservation reservation = new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                ReservationStatus.of(rs.getString("reservation_status"))
        );
        return new ReservationWaiting(
                rs.getLong("waiting_id"),
                rs.getString("waiting_name"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                reservation
        );
    }

    @Override
    public WaitingWithOrder save(ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setTimestamp(2, Timestamp.valueOf(reservationWaiting.getCreatedAt()));
            ps.setLong(3, reservationWaiting.getReservation().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return findWithOrderById(id)
                .orElseThrow(() -> new IllegalStateException("방금 저장한 대기를 찾을 수 없습니다. id=" + id));
    }

    @Override
    public boolean existBy(String name, Long reservationId) {
        String sql = "SELECT COUNT(*) FROM reservation_waiting WHERE name = ? AND reservation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, reservationId);
        return count != null && count > 0;
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = "SELECT " + WAITING_COLUMNS + FROM_WAITING + " WHERE rw.id = ?";
        List<ReservationWaiting> results = jdbcTemplate.query(sql, waitingRowMapper, id);
        return results.stream().findFirst();
    }

    private Optional<WaitingWithOrder> findWithOrderById(Long id) {
        String sql = "SELECT " + WAITING_COLUMNS + WAITING_ORDER_COLUMN + FROM_WAITING + " WHERE rw.id = ?";
        List<WaitingWithOrder> results = jdbcTemplate.query(sql, waitingWithOrderRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Optional<ReservationWaiting> findEarliestByReservationId(Long reservationId) {
        String sql = "SELECT " + WAITING_COLUMNS + FROM_WAITING
                + " WHERE rw.reservation_id = ? ORDER BY rw.created_at ASC, rw.id ASC LIMIT 1";
        List<ReservationWaiting> results = jdbcTemplate.query(sql, waitingRowMapper, reservationId);
        return results.stream().findFirst();
    }

    @Override
    public List<WaitingWithOrder> findByName(String name) {
        String sql = "SELECT " + WAITING_COLUMNS + WAITING_ORDER_COLUMN + FROM_WAITING
                + " WHERE rw.name = ? ORDER BY rw.created_at ASC, rw.id ASC";
        return jdbcTemplate.query(sql, waitingWithOrderRowMapper, name);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_waiting WHERE id = ?", id);
    }
}
