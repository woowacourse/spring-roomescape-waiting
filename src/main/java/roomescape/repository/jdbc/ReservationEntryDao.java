package roomescape.repository.jdbc;

import static roomescape.repository.jdbc.ReservationEntityMapper.RESERVATION_ENTRY_ROW_MAPPER;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationEntry;

@Repository
@RequiredArgsConstructor
public class ReservationEntryDao {

    private final JdbcTemplate jdbcTemplate;

    public List<ReservationEntry> saveAll(Long reservationId, List<ReservationEntry> entries) {
        return entries.stream()
                .map(entry -> save(reservationId, entry))
                .toList();
    }

    private ReservationEntry save(Long reservationId, ReservationEntry entry) {
        if (entry.getId() == null) {
            return insert(reservationId, entry);
        }
        update(entry);
        return entry;
    }

    private ReservationEntry insert(Long reservationId, ReservationEntry entry) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = """
                INSERT INTO reservation_entry (name, reservation_id, status, created_at)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entry.getName());
            ps.setLong(2, reservationId);
            ps.setString(3, entry.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(entry.getCreatedAt()));
            return ps;
        }, keyHolder);

        return ReservationEntry.from(
                keyHolder.getKey().longValue(),
                entry.getName(),
                entry.getStatus(),
                entry.getCreatedAt()
        );
    }

    private void update(ReservationEntry entry) {
        String sql = """
                UPDATE reservation_entry
                SET name = ?, status = ?, created_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                entry.getName(),
                entry.getStatus().name(),
                Timestamp.valueOf(entry.getCreatedAt()),
                entry.getId()
        );
    }

    public List<ReservationEntry> findByReservationId(Long reservationId) {
        String sql = """
                SELECT id AS entry_id, name AS entry_name, status AS entry_status, created_at AS entry_created_at
                FROM reservation_entry
                WHERE reservation_id = ?
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, RESERVATION_ENTRY_ROW_MAPPER, reservationId);
    }
}
