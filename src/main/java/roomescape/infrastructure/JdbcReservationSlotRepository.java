package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.ReservationSlotInfo;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationSlotInsert;
    private final SimpleJdbcInsert reservationInsert;

    private final RowMapper<ReservationSlotInfo> slotRowMapper = (rs, rowNum) -> new ReservationSlotInfo(
            rs.getLong("id"),
            rs.getDate("date").toLocalDate(),
            new Time(
                    rs.getLong("time_id"),
                    rs.getTime("time_value").toLocalTime()
            ),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail"),
                    rs.getLong("theme_amount")
            )
    );

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getLong("reservation_slot_id"),
            Status.valueOf(rs.getString("status")),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    public JdbcReservationSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationSlotInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_slot")
                .usingGeneratedKeyColumns("id");
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationSlot findByIdForUpdate(long id) {
        ReservationSlotInfo slot = findSlotByIdForUpdate(id);
        List<Reservation> reservations = findActiveReservationsBySlotId(id);
        return new ReservationSlot(slot, reservations);
    }

    @Override
    public ReservationSlot findByReservationIdForUpdate(long reservationId) {
        Long reservationSlotId = findReservationSlotIdByReservationId(reservationId);
        return findByIdForUpdate(reservationSlotId);
    }

    @Override
    public Long findSlotIdByReservationId(long reservationId) {
        String sql = """
                SELECT reservation_slot_id
                FROM reservation
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, reservationId);
    }

    private Long findReservationSlotIdByReservationId(long reservationId) {
        String sql = """
                SELECT reservation_slot_id
                FROM reservation
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, reservationId);
    }

    private ReservationSlotInfo findSlotByIdForUpdate(long id) {
        String sql = """
                SELECT rs.id,
                       rs.date,
                       t.id AS time_id,
                       t.start_at AS time_value,
                       th.id AS theme_id,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail,
                       th.amount AS theme_amount
                FROM reservation_slot rs
                JOIN reservation_time t ON rs.time_id = t.id
                JOIN theme th ON rs.theme_id = th.id
                WHERE rs.id = ?
                FOR UPDATE
                """;
        return jdbcTemplate.queryForObject(sql, slotRowMapper, id);
    }

    private List<Reservation> findActiveReservationsBySlotId(long slotId) {
        String sql = """
                SELECT id,
                       name,
                       reservation_slot_id,
                       status,
                       updated_at
                FROM reservation
                WHERE reservation_slot_id = ?
                  AND status != 'CANCELED'
                ORDER BY updated_at ASC, id ASC
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, slotId);
    }

    @Override
    public Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId
    ) {
        String sql = """
                SELECT r.id
                FROM reservation_slot r
                WHERE r.date = ?
                  AND r.time_id = ?
                  AND r.theme_id = ?
                """;
        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                date,
                timeId,
                themeId
        );
        return result.stream().findFirst();
    }

    @Override
    public List<ReservationSlotInfo> findAll() {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail,
                       th.amount AS theme_amount
                FROM reservation_slot AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, slotRowMapper);
    }

    @Override
    public Long save(LocalDate date, long timeId, long themeId) {
        return reservationSlotInsert.executeAndReturnKey(Map.of(
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
        )).longValue();
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        Long id = reservationInsert.executeAndReturnKey(Map.of(
                "name", reservation.getName(),
                "reservation_slot_id", reservation.getReservationSlotId(),
                "status", reservation.getStatus().name(),
                "updated_at", reservation.getUpdateAt()
        )).longValue();
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getReservationSlotId(),
                reservation.getStatus(),
                reservation.getUpdateAt()
        );
    }

    @Override
    public void updateReservation(Reservation reservation) {
        jdbcTemplate.update("""
                        UPDATE reservation
                        SET reservation_slot_id = ?,
                            status = ?,
                            updated_at = ?
                        WHERE id = ?
                        """,
                reservation.getReservationSlotId(),
                reservation.getStatus().name(),
                reservation.getUpdateAt(),
                reservation.getId()
        );
    }
}
