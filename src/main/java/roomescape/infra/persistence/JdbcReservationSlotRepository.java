package roomescape.infra.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.DuplicateException;
import roomescape.domain.reservation.ReservationSlotRepository;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ReservationSlot> findById(long id) {
        String sql = """
                SELECT
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation_slot rs
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE rs.id = ?
                """;
        List<ReservationSlot> reservationSlots = jdbcTemplate.query(sql, rowMapper(), id);
        return Optional.ofNullable(DataAccessUtils.singleResult(reservationSlots));
    }

    @Override
    public Optional<ReservationSlot> findByIdWithLock(long id) {
        String sql = """
                SELECT id
                FROM reservation_slot
                WHERE id = ?
                FOR UPDATE;
            """;

        List<Long> slotIds = jdbcTemplate.queryForList(sql, Long.class, id);
        if (slotIds.isEmpty()) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation_slot rs
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE rs.date = ?
                  AND rs.time_id = ?
                  AND rs.theme_id = ?
                """;
        List<ReservationSlot> reservationSlots = jdbcTemplate.query(sql, rowMapper(), date, timeId, themeId);
        return Optional.ofNullable(DataAccessUtils.singleResult(reservationSlots));
    }

    @Override
    public ReservationSlot save(ReservationSlot reservationSlot) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(reservationSlot);
        try {
            long id = insert.executeAndReturnKey(params).longValue();
            return new ReservationSlot(
                    id,
                    reservationSlot.getDate(),
                    reservationSlot.getTimeSlot(),
                    reservationSlot.getTheme()
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateException("이미 존재하는 예약 슬롯입니다.");
        }
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_slot")
                .usingColumns("date", "time_id", "theme_id")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(ReservationSlot reservationSlot) {
        return Map.of(
                "date", reservationSlot.getDate(),
                "time_id", reservationSlot.getTimeSlot().getId(),
                "theme_id", reservationSlot.getTheme().getId()
        );
    }

    private RowMapper<ReservationSlot> rowMapper() {
        return (rs, rowNum) -> new ReservationSlot(
                rs.getLong("slot_id"),
                rs.getObject("slot_date", LocalDate.class),
                new TimeSlot(
                        rs.getLong("time_id"),
                        rs.getObject("start_at", LocalTime.class)
                ),
                new Theme(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail_url"),
                        rs.getLong("theme_price")
                )
        );
    }
}
