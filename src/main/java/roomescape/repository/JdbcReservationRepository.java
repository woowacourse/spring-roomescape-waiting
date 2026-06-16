package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.ResourceConflictException;
import roomescape.service.exception.ResourceNotFoundException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String SELECT_BASE = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
                r.status AS reservation_status,
                s.id AS slot_id,
                d.date AS reservation_date,
                t.id AS time_id,
                t.start_at AS time_start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail
            FROM reservation r
            INNER JOIN reservation_slot s ON r.slot_id = s.id
            INNER JOIN reservation_date d ON s.date_id = d.id
            INNER JOIN reservation_time t ON s.time_id = t.id
            INNER JOIN theme th ON s.theme_id = th.id
            """;

    private static final String SELECT_BASE_WITH_WAITING_ORDER = """
            SELECT
                r.id           AS reservation_id,
                r.name         AS reservation_name,
                s.id           AS slot_id,
                d.date         AS reservation_date,
                t.id           AS time_id,
                t.start_at     AS time_start_at,
                th.id          AS theme_id,
                th.name        AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail,
                (SELECT COUNT(*)
                   FROM reservation r2
                  WHERE r2.slot_id = r.slot_id
                    AND r2.id < r.id) AS waiting_order
            FROM reservation r
            INNER JOIN reservation_slot s ON r.slot_id = s.id
            INNER JOIN reservation_date d ON s.date_id = d.id
            INNER JOIN reservation_time t ON s.time_id = t.id
            INNER JOIN theme th ON s.theme_id = th.id
            """;

    private final RowMapper<Reservation> reservationRowMapper =
            (rs, rowNum) -> new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    new ReservationSlot(
                            rs.getLong("slot_id"),
                            rs.getObject("reservation_date", LocalDate.class),
                            new ReservationTime(
                                    rs.getLong("time_id"),
                                    rs.getObject("time_start_at", LocalTime.class)
                            ),
                            new Theme(
                                    rs.getLong("theme_id"),
                                    rs.getString("theme_name"),
                                    rs.getString("theme_description"),
                                    rs.getString("theme_thumbnail")
                            )
                    ),
                    ReservationStatus.valueOf(rs.getString("reservation_status"))
            );

    private final RowMapper<ReservationWithWaitingOrder> reservationWithWaitingOrderRowMapper =
            (rs, rowNum) -> new ReservationWithWaitingOrder(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    new ReservationSlot(
                            rs.getLong("slot_id"),
                            rs.getObject("reservation_date", LocalDate.class),
                            new ReservationTime(
                                    rs.getLong("time_id"),
                                    rs.getObject("time_start_at", LocalTime.class)
                            ),
                            new Theme(
                                    rs.getLong("theme_id"),
                                    rs.getString("theme_name"),
                                    rs.getString("theme_description"),
                                    rs.getString("theme_thumbnail")
                            )
                    ),
                    rs.getLong("waiting_order")
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationWithWaitingOrder> findAll() {
        return jdbcTemplate.query(SELECT_BASE_WITH_WAITING_ORDER, reservationWithWaitingOrderRowMapper);
    }

    @Override
    public List<ReservationWithWaitingOrder> findByName(String name) {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.name = ?";
        return jdbcTemplate.query(sql, reservationWithWaitingOrderRowMapper, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = SELECT_BASE + " WHERE r.id = ?";
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationWithWaitingOrder save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (name, slot_id, status)
                SELECT ?, s.id, ?
                FROM reservation_slot s
                JOIN reservation_date d ON s.date_id = d.id
                WHERE d.date = ? AND s.time_id = ? AND s.theme_id = ?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setString(2, reservation.getStatus().name());
            ps.setDate(3, Date.valueOf(reservation.getSlot().getDate()));
            ps.setLong(4, reservation.getSlot().getTime().getId());
            ps.setLong(5, reservation.getSlot().getTheme().getId());
            return ps;
        }, keyHolder);

        if (affectedRows == 0) {
            throw new ResourceNotFoundException("해당 날짜, 시간 또는 테마를 가진 슬롯이 존재하지 않아 예약을 생성할 수 없습니다.");
        }

        Long id = keyHolder.getKey().longValue();
        return findWithWaitingOrderById(id);
    }

    @Override
    public ReservationWithWaitingOrder update(Reservation reservation) {
        String sql = """
                UPDATE reservation r
                SET r.name = ?,
                    r.slot_id = (SELECT s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = ? AND s.time_id = ? AND s.theme_id = ?)
                WHERE r.id = ?
                """;
        int affectedRows = jdbcTemplate.update(
                sql,
                reservation.getName(),
                Date.valueOf(reservation.getSlot().getDate()),
                reservation.getSlot().getTime().getId(),
                reservation.getSlot().getTheme().getId(),
                reservation.getId()
        );

        if (affectedRows == 0) {
            throw new ResourceNotFoundException("해당 예약이 존재하지 않거나 슬롯 정보가 올바르지 않습니다.");
        }

        return findWithWaitingOrderById(reservation.getId());
    }

    private ReservationWithWaitingOrder findWithWaitingOrderById(Long id) {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.id = ?";
        return jdbcTemplate.queryForObject(sql, reservationWithWaitingOrderRowMapper, id);
    }

    @Override
    public void confirm(Long id) {
        int affectedRows = jdbcTemplate.update(
                "UPDATE reservation SET status = ? WHERE id = ? AND status = ?",
                ReservationStatus.CONFIRMED.name(), id, ReservationStatus.PENDING.name()
        );
        if (affectedRows == 0) {
            throw new ResourceConflictException(
                    "결제 대기(PENDING) 상태의 예약이 아니어서 확정할 수 없습니다: reservationId=" + id);
        }
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = ?)",
                Boolean.class,
                id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation r
                    JOIN reservation_slot s ON r.slot_id = s.id
                    JOIN reservation_date d ON s.date_id = d.id
                    WHERE r.name = ? AND d.date = ? AND s.time_id = ? AND s.theme_id = ?
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                name, Date.valueOf(date), timeId, themeId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation r
                    JOIN reservation_slot s ON r.slot_id = s.id
                    JOIN reservation_date d ON s.date_id = d.id
                    WHERE d.date = ? AND s.time_id = ? AND s.theme_id = ?
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                Date.valueOf(date), timeId, themeId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation r
                    JOIN reservation_slot s ON r.slot_id = s.id
                    JOIN reservation_date d ON s.date_id = d.id
                    WHERE d.date = ? AND s.time_id = ? AND s.theme_id = ? AND r.id <> ?
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                Date.valueOf(date), timeId, themeId, id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation r JOIN reservation_slot s ON r.slot_id = s.id WHERE s.time_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation r JOIN reservation_slot s ON r.slot_id = s.id WHERE s.theme_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
        return Boolean.TRUE.equals(exists);
    }
}
