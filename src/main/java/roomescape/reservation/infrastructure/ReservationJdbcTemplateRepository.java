package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class ReservationJdbcTemplateRepository implements ReservationRepository {

    private static final String FIND_BY_DATE_AND_THEME_ID_QUERY = """
        SELECT r.id,
               r.name AS reservation_name,
               r.date,
               r.created_at,
               rt.id AS time_id,
               rt.start_at,
               t.id AS theme_id,
               t.name AS theme_name,
               t.description AS theme_description,
               t.thumbnail_url
        FROM reservation r
        JOIN reservation_time rt ON r.time_id = rt.id
        JOIN theme t ON r.theme_id = t.id
        WHERE (? IS NULL OR r.date = ?)
          AND (? IS NULL OR r.theme_id = ?)
        ORDER BY r.id
        """;
    private static final String FIND_BY_ID_QUERY = """
        SELECT r.id,
               r.name AS reservation_name,
               r.date,
               r.created_at,
               rt.id AS time_id,
               rt.start_at,
               t.id AS theme_id,
               t.name AS theme_name,
               t.description AS theme_description,
               t.thumbnail_url
        FROM reservation r
        JOIN reservation_time rt ON r.time_id = rt.id
        JOIN theme t ON r.theme_id = t.id
        WHERE r.id = ?
        """;
    private static final String FIND_BY_DATE_AND_TIME_ID_AND_THEME_ID_QUERY = """
        SELECT r.id,
               r.name AS reservation_name,
               r.date,
               r.created_at,
               rt.id AS time_id,
               rt.start_at,
               t.id AS theme_id,
               t.name AS theme_name,
               t.description AS theme_description,
               t.thumbnail_url
        FROM reservation r
        JOIN reservation_time rt ON r.time_id = rt.id
        JOIN theme t ON r.theme_id = t.id
        WHERE r.date = ?
          AND r.time_id = ?
          AND r.theme_id = ?
        """;
    private static final String FIND_ALL_QUERY = """
        SELECT r.id,
               r.name AS reservation_name,
               r.date,
               r.created_at,
               rt.id AS time_id,
               rt.start_at,
               t.id AS theme_id,
               t.name AS theme_name,
               t.description AS theme_description,
               t.thumbnail_url
        FROM reservation r
        JOIN reservation_time rt ON r.time_id = rt.id
        JOIN theme t ON r.theme_id = t.id
        ORDER BY r.id
        """;
    private static final String EXISTS_BY_TIME_ID_QUERY = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE time_id = ?
        );
        """;
    private static final String EXISTS_BY_THEME_ID_QUERY = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE theme_id = ?
        );
        """;
    private static final String UPDATE_SCHEDULE_BY_ID_AND_NAME_QUERY = """
        UPDATE reservation
        SET date = ?, time_id = ?
        WHERE id = ?
          AND name = ?
        """;
    private static final String FIND_BY_NAME_QUERY = """
        SELECT r.id,
               r.name AS reservation_name,
               r.date,
               r.created_at,
               rt.id AS time_id,
               rt.start_at,
               t.id AS theme_id,
               t.name AS theme_name,
               t.description AS theme_description,
               t.thumbnail_url
        FROM reservation r
        JOIN reservation_time rt ON r.time_id = rt.id
        JOIN theme t ON r.theme_id = t.id
        WHERE r.name = ?
        """;
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM reservation WHERE id = ?";
    private static final String DELETE_BY_ID_AND_NAME_QUERY = "DELETE FROM reservation WHERE id = ? AND name = ?";
    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.createRow(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );

        Theme theme = Theme.createRow(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("thumbnail_url")
        );

        return Reservation.createRow(
                rs.getLong("id"),
                rs.getString("reservation_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationJdbcTemplateRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> params = Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId(),
                "created_at", reservation.getCreatedAt()
        );
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return reservation.appendId(id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        List<Reservation> reservation = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                ROW_MAPPER,
                id
        );
        return reservation.stream()
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Reservation> reservation = jdbcTemplate.query(
                FIND_BY_DATE_AND_TIME_ID_AND_THEME_ID_QUERY,
                ROW_MAPPER,
                date,
                timeId,
                themeId
        );
        return reservation.stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, ROW_MAPPER);
    }

    @Override
    public boolean existsByReservationTimeId(Long id) {
        Integer result = jdbcTemplate.queryForObject(EXISTS_BY_TIME_ID_QUERY, Integer.class, id);
        return result != null && result == 1;
    }

    @Override
    public boolean existsByThemeId(Long id) {
        Integer result = jdbcTemplate.queryForObject(EXISTS_BY_THEME_ID_QUERY, Integer.class, id);
        return result != null && result == 1;
    }

    @Override
    public void updateSchedule(Reservation updateReservation) {
        jdbcTemplate.update(
                UPDATE_SCHEDULE_BY_ID_AND_NAME_QUERY,
                updateReservation.getDate(),
                updateReservation.getTime().getId(),
                updateReservation.getId(),
                updateReservation.getName()
        );
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jdbcTemplate.query(
                FIND_BY_DATE_AND_THEME_ID_QUERY,
                ROW_MAPPER,
                date,
                date,
                themeId,
                themeId
        );
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(
                FIND_BY_NAME_QUERY,
                ROW_MAPPER,
                name
        );
    }


    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID_QUERY, id);
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        jdbcTemplate.update(
                DELETE_BY_ID_AND_NAME_QUERY,
                id,
                name
        );
    }
}
