package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationRequestLockRepository;

@Repository
public class JdbcReservationRequestLockRepository implements ReservationRequestLockRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRequestLockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void lock(String name, LocalDate date, Long timeId) {
        String mergeSql = """
                MERGE INTO reservation_request_lock (name, reservation_date, time_id)
                KEY (name, reservation_date, time_id)
                VALUES (?, ?, ?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(mergeSql);
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(date));
            ps.setLong(3, timeId);
            return ps;
        });

        String lockSql = """
                SELECT id
                FROM reservation_request_lock
                WHERE name = ? AND reservation_date = ? AND time_id = ?
                FOR UPDATE
                """;
        jdbcTemplate.queryForList(lockSql, Integer.class, name, Date.valueOf(date), timeId);
    }
}
