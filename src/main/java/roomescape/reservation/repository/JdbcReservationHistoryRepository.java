package roomescape.reservation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.InfrastructureException;
import roomescape.reservation.domain.ReservationHistory;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.util.List;

@Repository
public class JdbcReservationHistoryRepository implements ReservationHistoryRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReservationHistoryRepository.class);

    private final RowMapper<ReservationHistory> reservationHistoryRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );
        Slot reservationSlot = new Slot(
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );

        return new ReservationHistory(
                resultSet.getLong("reservation_id"),
                resultSet.getString("name"),
                reservationSlot,
                resultSet.getLong("request_order"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("canceled_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationHistory> findByName(String name) {
        String sql = """
                SELECT h.reservation_id,
                       h.name,
                       h.date,
                       h.time_id,
                       rt.start_at,
                       h.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       h.request_order,
                       h.created_at,
                       h.canceled_at
                FROM reservation_history h
                JOIN reservation_time rt ON h.time_id = rt.id
                JOIN theme t ON h.theme_id = t.id
                WHERE h.name = ?
                ORDER BY h.date ASC,
                         rt.start_at ASC,
                         h.request_order ASC,
                         h.reservation_id ASC
                """;

        return jdbcTemplate.query(sql, reservationHistoryRowMapper, name);
    }

    @Override
    public boolean saveFromReservation(Long reservationId) {
        String sql = """
                INSERT INTO reservation_history (
                    reservation_id,
                    name,
                    date,
                    time_id,
                    theme_id,
                    request_order,
                    created_at,
                    canceled_at
                )
                SELECT id,
                       name,
                       date,
                       time_id,
                       theme_id,
                       request_order,
                       created_at,
                       CURRENT_TIMESTAMP
                FROM reservation
                WHERE id = ?
                """;

        int rowCount = jdbcTemplate.update(sql, reservationId);
        if (rowCount == 0) {
            return false;
        }
        if (rowCount != 1) {
            log.error(
                    "Reservation history insert affected unexpected row count. rowCount={}, reservationId={}",
                    rowCount,
                    reservationId
            );
            throw new InfrastructureException("예약 취소 이력 저장에 실패했습니다.");
        }

        return true;
    }
}
