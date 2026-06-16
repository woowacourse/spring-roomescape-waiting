package roomescape.infra;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.CompletedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.domain.reservationStatus.ReservationStatus;
import roomescape.repository.ThemeSlotRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeSlotRepository implements ThemeSlotRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme_slot")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ThemeSlot save(ThemeSlot themeSlot) {
        Map<String, Object> params = createParams(themeSlot);
        long themeSlotId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return ThemeSlot.of(themeSlotId, themeSlot);
    }

    @Override
    public List<ThemeSlot> saveAll(List<ThemeSlot> themeSlots) {
        String sql = """
                INSERT INTO theme_slot 
                (theme_id, date, time_id, is_reserved)
                VALUES (?, ?, ?, ?);                
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.batchUpdate(
                con -> con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ThemeSlot slot = themeSlots.get(i);
                        ps.setLong(1, slot.getTheme().getId());
                        ps.setObject(2, slot.getDate());
                        ps.setLong(3, slot.getTime().getId());
                        ps.setBoolean(4, slot.isReserved());
                    }

                    @Override
                    public int getBatchSize() {
                        return themeSlots.size();
                    }
                },
                keyHolder
        );

        List<Long> keys = keyHolder.getKeyList().stream()
                .map(m -> ((Number) m.values().iterator().next()).longValue())
                .toList();

        List<ThemeSlot> results = new ArrayList<>();
        for (int i = 0; i < themeSlots.size(); i++) {
            results.add(ThemeSlot.of(keys.get(i), themeSlots.get(i)));
        }
        return results;
    }

    @Override
    public boolean isExistBy(long themeId, LocalDate date) {
        String sql = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM theme_slot 
                            WHERE theme_id = ? 
                            AND date = ?
                        ) 
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date));
    }

    @Override
    public List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date) {
        String sql = """
                SELECT
                    ts.id AS id,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price,
                    ts.date AS date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    ts.is_reserved AS is_reserved
                FROM
                    theme_slot ts
                        INNER JOIN time t ON ts.time_id = t.id
                        INNER JOIN theme th ON ts.theme_id = th.id
                WHERE ts.theme_id = ?
                AND ts.date = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), themeId, date);
    }

    @Override
    public Optional<ThemeSlot> findById(long id) {
        String sql = """
                SELECT
                    ts.id AS id,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price,
                    ts.date AS date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    ts.is_reserved AS is_reserved
                FROM
                    theme_slot ts
                        INNER JOIN time t ON ts.time_id = t.id
                        INNER JOIN theme th ON ts.theme_id = th.id
                WHERE ts.id = ?
                FOR UPDATE
                """;
        return jdbcTemplate.query(sql, rowMapper(), id).stream().findFirst();
    }

    private Map<String, Object> createParams(ThemeSlot themeSlot) {
        return Map.of(
                "theme_id", themeSlot.getTheme().getId(),
                "date", themeSlot.getDate(),
                "time_id", themeSlot.getTime().getId(),
                "is_reserved", themeSlot.isReserved()
        );
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme_slot WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void update(ThemeSlot themeSlot) {
        String sql = """
                UPDATE theme_slot 
                SET is_reserved = ? 
                WHERE theme_id = ?
                AND date = ?
                AND time_id = ?
                """;

        jdbcTemplate.update(sql,
                themeSlot.isReserved(),
                themeSlot.getTheme().getId(),
                themeSlot.getDate(),
                themeSlot.getTime().getId()
        );
    }

    @Override
    public Optional<ThemeSlot> findWithReservations(Long themeSlotId) {
        Optional<ThemeSlot> themeSlotOpt = findById(themeSlotId);
        if (themeSlotOpt.isEmpty()) {
            return Optional.empty();
        }
        ThemeSlot themeSlot = themeSlotOpt.get();

        List<Reservation> reservations = jdbcTemplate.query("""
                        SELECT r.id AS r_id, 
                               r.name AS r_name, 
                               r.status AS r_status
                        FROM reservation r
                        WHERE r.theme_slot_id = ?
                        """,
                (rs, rowNum) -> new Reservation(
                        rs.getLong("r_id"),
                        rs.getString("r_name"),
                        themeSlotId,
                        themeSlot.getDate(),
                        themeSlot.getTime(),
                        themeSlot.getTheme(),
                        toReservationStatus(rs.getString("r_status"))
                ),
                themeSlotId
        );

        return Optional.of(new ThemeSlot(
                themeSlot.getId(), themeSlot.getTheme(), themeSlot.getDate(),
                themeSlot.getTime(), themeSlot.isReserved(), reservations
        ));
    }

    private ReservationStatus toReservationStatus(String status) {
        return switch (status) {
            case "PENDING" -> PendingStatus.getInstance();
            case "CONFIRMED" -> ConfirmedStatus.getInstance();
            case "COMPLETED" -> CompletedStatus.getInstance();
            case "CANCELLED" -> CancelledStatus.getInstance();
            default -> throw new IllegalArgumentException("존재하지 않는 예약 상태입니다.");
        };
    }

    private RowMapper<ThemeSlot> rowMapper() {
        return (rs, rowNum) -> new ThemeSlot(
                rs.getLong("id"),
                new Theme(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail_url"),
                        rs.getLong("theme_price")
                ),
                rs.getObject("date", LocalDate.class),
                new Time(
                        rs.getLong("time_id"),
                        rs.getObject("start_at", LocalTime.class)),
                rs.getBoolean("is_reserved")
        );
    }
}
