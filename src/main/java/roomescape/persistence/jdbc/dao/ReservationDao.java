package roomescape.persistence.jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.persistence.jdbc.mapper.ReservationRowMapper;
import roomescape.persistence.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;

    public List<Reservation> saveAll(Long slotId, List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> save(slotId, reservation))
                .toList();
    }

    private Reservation save(Long slotId, Reservation reservation) {
        if (reservation.getId() == null) {
            return insert(slotId, reservation);
        }
        update(reservation);
        return reservation;
    }

    private Reservation insert(Long slotId, Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = """
                INSERT INTO reservation (name, slot_id, status, created_at)
                VALUES (?, ?, ?, ?)
                """;

        RepositoryExceptionTranslator.execute(() ->
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                    ps.setString(1, reservation.getName());
                    ps.setLong(2, slotId);
                    ps.setString(3, reservation.getStatus().name());
                    ps.setTimestamp(4, Timestamp.valueOf(reservation.getCreatedAt()));
                    return ps;
                }, keyHolder), "이미 예약 또는 대기가 존재합니다.");

        return new Reservation(
                keyHolder.getKey().longValue(),
                reservation.getName(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }

    private void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?, status = ?, created_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getStatus().name(),
                Timestamp.valueOf(reservation.getCreatedAt()),
                reservation.getId()
        );
    }

    public List<Reservation> findBySlotId(Long slotId) {
        String sql = """
                SELECT id AS reservation_id, name AS reservation_name, status AS reservation_status, created_at AS reservation_created_at
                FROM reservation
                WHERE slot_id = ?
                ORDER BY created_at
                """;
        return jdbcTemplate.query(sql, ReservationRowMapper.RESERVATION_ROW_MAPPER, slotId);
    }
}
