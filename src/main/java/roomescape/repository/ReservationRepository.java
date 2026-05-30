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
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

@Repository
public class ReservationRepository {
    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> Reservation.load(
            resultSet.getLong("reservation_id"),
            new ReservationName(resultSet.getString("name")),
            new ReservationDate(resultSet.getDate("date").toLocalDate()),
            ReservationTime.of(resultSet.getLong("time_id"), resultSet.getTime("start_at").toLocalTime()),
            Theme.load(resultSet.getLong("theme_id"), new ThemeName(resultSet.getString("theme_name")),
                    resultSet.getString("description"), new ThumbnailUrl(resultSet.getString("thumbnail_url"))),
            resultSet.getTimestamp("created_at").toLocalDateTime());
    private static final String SELECT_ALL = """
            SELECT r.id   AS reservation_id,
                   r.name,
                   r.date,
                   r.created_at,
                   rt.id  AS time_id,
                   rt.start_at,
                   t.id   AS theme_id,
                   t.name AS theme_name,
                   t.description,
                   t.thumbnail_url
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
                    created_at = ?
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
                "date", reservation.getDate().getDate(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId(),
                "created_at", reservation.getDateTime()
        );

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Reservation.load(generatedKey,
                reservation.getName(),
                reservation.getDate(), reservation.getTime(),
                reservation.getTheme()
                , reservation.getDateTime());
    }

    public Reservation update(long id, Reservation target) {
        jdbcTemplate.update(UPDATE, target.getName().getValue(), target.getDate().getDate(), target.getTime().getId(),
                target.getTheme().getId(), target.getDateTime(), id);

        return Reservation.load(id, target.getName(), target.getDate(), target.getTime(), target.getTheme(),
                target.getDateTime());
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
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
                jdbcTemplate.queryForObject(EXISTS_BY_DATE_AND_TIME_AND_THEME_ID, Boolean.class, date, timeId,
                        themeId, name));
    }

    public List<Reservation> findByTimeAndThemeAndDate(ReservationTime time, Theme theme, ReservationDate date) {
        String sql = """
                SELECT r.id   AS reservation_id,
                       r.name,
                       r.date,
                       r.created_at,
                       rt.id  AS time_id,
                       rt.start_at,
                       t.id   AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id  = rt.id
                INNER JOIN theme             t  ON r.theme_id = t.id
                WHERE date = ? AND t.id = ? AND rt.id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date.getDate(), theme.getId(), time.getId());
    }
}
