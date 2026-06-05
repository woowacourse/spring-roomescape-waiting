package roomescape.reservationhistory;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.Reservation;

@Repository
public class ReservationHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationHistory insert(ReservationHistory history) {
        String sql = "insert into reservation_history "
                + "(reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setLong(1, history.getReservationId());
            ps.setLong(2, history.getMemberId());
            ps.setString(3, history.getDate().toString());
            ps.setLong(4, history.getTimeId());
            ps.setLong(5, history.getThemeId());
            ps.setLong(6, history.getStoreId());
            ps.setString(7, history.getStatus().name());
            ps.setLong(8, history.getActorId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new ReservationHistory(
                id,
                history.getReservationId(),
                history.getMemberId(),
                history.getDate(),
                history.getTimeId(),
                history.getThemeId(),
                history.getStoreId(),
                history.getStatus(),
                history.getActorId(),
                history.getCreatedAt()
        );
    }

    public int updateSnapshot(Reservation reservation) {
        String sql = "update reservation_history "
                + "set date = ?, time_id = ?, theme_id = ?, store_id = ? "
                + "where reservation_id = ? and member_id = ?";
        return jdbcTemplate.update(
                sql,
                reservation.getDate().toString(),
                reservation.getTime().getId(),
                reservation.getThemeId(),
                reservation.getStoreId(),
                reservation.getId(),
                reservation.getMemberId()
        );
    }

    public int markCanceled(Long reservationId, Long memberId) {
        String sql = "update reservation_history "
                + "set status = 'CANCELED' "
                + "where reservation_id = ? and member_id = ?";
        return jdbcTemplate.update(sql, reservationId, memberId);
    }

    public List<ReservationHistory> findByReservationId(Long reservationId) {
        String sql = "select id, reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id, created_at "
                + "from reservation_history "
                + "where reservation_id = ? "
                + "order by created_at, id";
        return jdbcTemplate.query(sql, historyRowMapper(), reservationId);
    }

    public List<ReservationHistory> findByMemberId(Long memberId) {
        String sql = "select id, reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id, created_at "
                + "from reservation_history "
                + "where member_id = ? "
                + "order by created_at, id";
        return jdbcTemplate.query(sql, historyRowMapper(), memberId);
    }

    public List<ReservationHistory> findByStoreId(Long storeId) {
        String sql = "select id, reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id, created_at "
                + "from reservation_history "
                + "where store_id = ? "
                + "order by created_at, id";
        return jdbcTemplate.query(sql, historyRowMapper(), storeId);
    }

    private RowMapper<ReservationHistory> historyRowMapper() {
        return (resultSet, rowNum) -> new ReservationHistory(
                resultSet.getLong("id"),
                resultSet.getLong("reservation_id"),
                resultSet.getLong("member_id"),
                LocalDate.parse(resultSet.getString("date")),
                resultSet.getLong("time_id"),
                resultSet.getLong("theme_id"),
                resultSet.getLong("store_id"),
                ReservationHistoryStatus.valueOf(resultSet.getString("status")),
                resultSet.getLong("actor_id"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
