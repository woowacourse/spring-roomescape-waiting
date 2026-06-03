package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationRepository {
    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER =
            (rs, rowNum) -> RepositoryRowMapper.reservationRowMapper(rs);
    private static final String SELECT_ALL = """
            SELECT r.id         AS reservation_id,
                   r.name,
                   r.status,
                   r.created_at,
                   s.id         AS slot_id,
                   s.date       AS slot_date,
                   rt.id        AS time_id,
                   rt.start_at,
                   t.id         AS theme_id,
                   t.name       AS theme_name,
                   t.description,
                   t.thumbnail_url
            FROM reservation r
            INNER JOIN slot             s  ON r.slot_id  = s.id
            INNER JOIN reservation_time rt ON s.time_id  = rt.id
            INNER JOIN theme             t  ON s.theme_id = t.id
            """;
    private static final String UPDATE = """
            UPDATE reservation
                SET name = ?, slot_id = ?, created_at = ?
            WHERE id = ?
            """;
    private static final String SELECT_BY_ID = SELECT_ALL + "WHERE r.id = ?";
    private static final String SELECT_BY_NAME = SELECT_ALL + "WHERE r.name = ?";
    private static final String EXISTS_BY_DATE_AND_TIME_AND_THEME_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation r
                INNER JOIN slot s ON r.slot_id = s.id
                WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ? AND r.name = ?
            )
            """;
    private static final String EXISTS_APPROVED_BY_SLOT = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation r
                INNER JOIN slot s ON r.slot_id = s.id
                WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ? AND r.status = 'APPROVED'
            )
            """;
    private static final String SELECT_FIRST_WAITING_BY_SLOT = SELECT_ALL + """
            WHERE s.date = ? AND rt.id = ? AND t.id = ? AND r.status = 'WAITING'
            ORDER BY r.created_at, r.id
            LIMIT 1
            """;
    private static final String UPDATE_STATUS = "UPDATE reservation SET status = ? WHERE id = ?";
    private static final String EXISTS_BY_TIME_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation r
                INNER JOIN slot s ON r.slot_id = s.id
                WHERE s.time_id = ?
            )
            """;
    private static final String EXISTS_BY_THEME_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation r
                INNER JOIN slot s ON r.slot_id = s.id
                WHERE s.theme_id = ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(SELECT_ALL, RESERVATION_ROW_MAPPER);
    }

    public List<Reservation> findAllByName(String reservationName) {
        return jdbcTemplate.query(SELECT_BY_NAME, RESERVATION_ROW_MAPPER, reservationName);
    }

    public Optional<Reservation> findById(long reservationId) {
        List<Reservation> result = jdbcTemplate.query(SELECT_BY_ID, RESERVATION_ROW_MAPPER, reservationId);
        return result.stream().findFirst();
    }

    public Reservation save(Reservation reservation) {
        Map<String, Object> params = Map.of(
                "name", reservation.getName().getValue(),
                "slot_id", reservation.getSlot().getId(),
                "status", reservation.getStatus().name(),
                "created_at", reservation.getCreatedAt()
        );

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Reservation.load(generatedKey, reservation.getName(), reservation.getSlot(),
                reservation.getStatus(), reservation.getCreatedAt());
    }

    public Reservation update(long id, Reservation target) {
        jdbcTemplate.update(UPDATE, target.getName().getValue(), target.getSlot().getId(),
                target.getCreatedAt(), id);

        return Reservation.load(id, target.getName(), target.getSlot(),
                target.getStatus(), target.getCreatedAt());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public boolean existsByTimeId(long reservationTimeId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_TIME_ID, Boolean.class, reservationTimeId));
    }

    public boolean existsByThemeId(long themeId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_THEME_ID, Boolean.class, themeId));
    }

    public boolean existsByTimeAndThemeAndDateAndName(Long timeId, Long themeId, LocalDate date, String name) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_DATE_AND_TIME_AND_THEME_ID, Boolean.class,
                        date, timeId, themeId, name));
    }

    public boolean existsApprovedByTimeAndThemeAndDate(Long timeId, Long themeId, LocalDate date) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_APPROVED_BY_SLOT, Boolean.class, date, timeId, themeId));
    }

    public Optional<Reservation> findFirstWaitingByTimeAndThemeAndDate(Long timeId, Long themeId, LocalDate date) {
        List<Reservation> result = jdbcTemplate.query(SELECT_FIRST_WAITING_BY_SLOT, RESERVATION_ROW_MAPPER,
                date, timeId, themeId);
        return result.stream().findFirst();
    }

    public void updateStatus(Long id, Status status) {
        jdbcTemplate.update(UPDATE_STATUS, status.name(), id);
    }

    public List<Reservation> findByTimeAndThemeAndDate(ReservationTime time, Theme theme, ReservationDate date) {
        String sql = SELECT_ALL + "WHERE s.date = ? AND t.id = ? AND rt.id = ?";
        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date.getValue(), theme.getId(), time.getId());
    }
}
