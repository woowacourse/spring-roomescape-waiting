package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> Reservation.of(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.of(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_start_at").toLocalTime(),
                    resultSet.getTime("time_finish_at").toLocalTime()
            ),
            Theme.of(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_image_url")
            )
    );

    private final RowMapper<Long> timeMapper = (resultSet, rowNum) ->
            resultSet.getLong("time_id");

    private final RowMapper<Long> idRowMapper = (resultSet, rowNum)
            -> resultSet.getLong("theme_id");

    private final RowMapper<Reservation> myReservationMapper = (resultSet, rowNum) -> Reservation.of(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.of(null, resultSet.getTime("time_start_at").toLocalTime(), null),
            Theme.of(null, resultSet.getString("theme_name"), null, null)
    );

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.of(id, reservation.getName(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String query = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    public List<Long> findTimeByDateAndThemeId(LocalDate date, Long themeId) {
        String query = """
                SELECT r.time_id
                FROM reservation r
                WHERE r.date = ? AND r.theme_id = ?
                """;
        return jdbcTemplate.query(query, timeMapper, date, themeId);
    }

    public void deleteById(Long id) {
        String query = "delete from reservation where id = ?";
        jdbcTemplate.update(query, id);
    }

    public boolean existsByThemeId(Long themeId) {
        String query = """
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, themeId);
        return count != null && count > 0;
    }

    public List<Long> findThemeIdTop10(LocalDate startDate, LocalDate endDate) {
        String query = """
                SELECT r.theme_id AS theme_id
                FROM reservation r
                WHERE r.date BETWEEN ? AND ?
                GROUP BY r.theme_id
                ORDER BY COUNT(r.id) DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(query, idRowMapper, startDate, endDate);
    }

    public List<Reservation> findByName(String name) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date,
                       t.start_at AS time_start_at,
                       th.name AS theme_name
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.name = ?
                """;

        return jdbcTemplate.query(query, myReservationMapper, name);
    }

    public Optional<Reservation> findById(Long id) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(query, rowMapper, id).stream()
                .findFirst();
    }

    public Optional<Reservation> findByIdForUpdate(Long id) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                FOR UPDATE
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream()
                .findFirst();
    }

    public Optional<String> findNameByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String query = """
                SELECT name
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("name"), date, timeId, themeId)
                .stream()
                .findFirst();
    }

    public void updateDateAndTime(Long id, LocalDate date, Long timeId) {
        String query = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(query, date, timeId, id);
    }
}
