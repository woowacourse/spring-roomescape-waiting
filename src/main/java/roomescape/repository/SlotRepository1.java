package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

@Repository
public class ReservationRepository1 {
    public static final RowMapper<Slot> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> Slot.load(
            resultSet.getLong("reservation_id"),
            new ReservationName(resultSet.getString("name")),
            new ReservationDate(resultSet.getDate("date").toLocalDate()),
            ReservationTime.of(resultSet.getLong("time_id"), resultSet.getTime("start_at").toLocalTime()),
            Theme.load(resultSet.getLong("theme_id"), new ThemeName(resultSet.getString("theme_name")),
                    resultSet.getString("description"), new ThumbnailUrl(resultSet.getString("thumbnail_url"))),
            Status.valueOf(resultSet.getString("status")),
            resultSet.getInt("rank"));

    private static final String SELECT_ALL = """
            SELECT r.id   AS reservation_id,
                   r.name,
                   r.date,
                   r.status,
                   rt.id  AS time_id,
                   rt.start_at,
                   t.id   AS theme_id,
                   t.name AS theme_name,
                   t.description,
                   t.thumbnail_url,
                   ROW_NUMBER() OVER (PARTITION BY r.date, r.time_id, r.theme_id, r.status ORDER BY r.id) AS rank
            FROM reservation r
            INNER JOIN reservation_time rt ON r.time_id  = rt.id
            INNER JOIN theme             t  ON r.theme_id = t.id
            """;
    private static final String UPDATE = """
            UPDATE reservation
                SET
                    name = ?,
                    date = ?,
                    time_id = ?,
                    theme_id = ?,
                    status = ?
            WHERE id = ?
            """;
    private static final String SELECT_BY_ID = SELECT_ALL + "WHERE r.id = ?";
    private static final String SELECT_BY_NAME = SELECT_ALL + "WHERE r.name = ?";
    private static final String EXISTS_BY_DATE_AND_TIME_AND_THEME_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
            )
            """;
    private static final String EXISTS_BY_DATE_AND_TIME_AND_THEME_ID_EXCLUDING_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ? AND id != ?
            )
            """;
    private static final String EXISTS_BY_TIME_ID = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                    )
            """;
    private static final String EXISTS_BY_THEME_ID = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                    )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationRepository1(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Slot> findAll() {
        return jdbcTemplate.query(SELECT_ALL, RESERVATION_ROW_MAPPER);
    }

    public List<Slot> findAllByName(String reservationName) {
        return jdbcTemplate.query(SELECT_BY_NAME, RESERVATION_ROW_MAPPER, reservationName);
    }

    public Optional<Slot> findById(long reservationId) {
        List<Slot> result = jdbcTemplate.query(SELECT_BY_ID, RESERVATION_ROW_MAPPER, reservationId);
        return result.stream().findFirst();
    }

    public Slot save(Slot reservation) {
        Map<String, Object> params = Map.of(
                "name", reservation.getName().getValue(),
                "date", reservation.getDate().getDate(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId(),
                "status", reservation.getStatus().name()
        );
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return findById(generatedKey).orElseThrow();
    }

    public Slot update(long id, Slot target) {
        jdbcTemplate.update(UPDATE,
                target.getName().getValue(), target.getDate().getDate(), target.getTime().getId(),
                target.getTheme().getId(), target.getStatus().name(), id);
        return findById(id).orElseThrow();
    }

    public void updateStatusById(long id, Status status) {
        jdbcTemplate.update("UPDATE reservation SET status = ? WHERE id = ?", status.name(), id);
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
                jdbcTemplate.queryForObject(EXISTS_BY_DATE_AND_TIME_AND_THEME_ID, Boolean.class, date, timeId, themeId, name));
    }

    public boolean existsByTimeAndThemeAndDateAndNameExcludingId(Long timeId, Long themeId, LocalDate date, String name, long excludeId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_DATE_AND_TIME_AND_THEME_ID_EXCLUDING_ID, Boolean.class, date, timeId, themeId, name, excludeId));
    }

    public boolean existsApprovedByTimeAndThemeAndDate(long timeId, long themeId, LocalDate date) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation
                    WHERE date = ? AND time_id = ? AND theme_id = ? AND status = 'APPROVED'
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId));
    }

    public Optional<Slot> findFirstWaitingByTimeAndThemeAndDate(ReservationTime time, Theme theme, ReservationDate date) {
        String sql = SELECT_ALL + "WHERE r.date = ? AND t.id = ? AND rt.id = ? AND r.status = 'WAITING' ORDER BY r.id ASC LIMIT 1";
        List<Slot> result = jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date.getDate(), theme.getId(), time.getId());
        return result.stream().findFirst();
    }
}
