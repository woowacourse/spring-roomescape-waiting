package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        return new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("reservation_date").toLocalDate(),
                time,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getReservationTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservation.updateId(id);
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE r.name = ?
        ORDER BY reservation_date ASC, time_start_at ASC, reservation_id ASC
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, name);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(Long id) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE r.id = ?
        FOR UPDATE
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId, Long themeId) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE  r.reservation_date = ? AND r.theme_id = ? AND r.time_id = ?
        FOR UPDATE
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date, themeId, timeId)
                .stream().findFirst();
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE reservation_date = ? AND theme_id = ? AND time_id = ?
        )
        """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, themeId, timeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        ORDER BY reservation_date ASC, time_start_at ASC, reservation_id ASC
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<PopularThemeQueryResult> findPopularThemes(LocalDate from, LocalDate to, int limit) {
        String sql = """
        SELECT t.id,
               t.name,
               t.description,
               t.thumbnail_url
        FROM reservation r
        INNER JOIN theme t
          ON r.theme_id = t.id
        WHERE r.reservation_date >= ?
          AND r.reservation_date <= ?
        GROUP BY t.id,
                 t.name,
                 t.description,
                 t.thumbnail_url
        ORDER BY COUNT(r.id) DESC,
                 t.id ASC
        LIMIT ?
        """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new PopularThemeQueryResult(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail_url")
                ),
                Date.valueOf(from),
                Date.valueOf(to),
                limit
        );
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeIdExceptId(LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE reservation_date = ?  AND theme_id = ? AND time_id = ? AND id != ?
        )
        """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, themeId, timeId, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?, reservation_date = ?, time_id = ?, theme_id = ?
                WHERE id = ?
                """;

        int affectedRow = jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );

        if (affectedRow == 0) {
            throw new ReservationNotFoundException();
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
               DELETE FROM reservation
               WHERE id = ?
               """;

        return jdbcTemplate.update(sql, id);
    }
}
