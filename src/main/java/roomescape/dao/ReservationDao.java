package roomescape.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
@Transactional(readOnly = true)
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(
                """
                            SELECT r.id,r.name,r.date,rt.id AS time_id, rt.start_at,
                            t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                            r.status
                            FROM reservation r
                            INNER JOIN reservation_time rt ON r.time_id = rt.id
                            INNER JOIN theme t ON r.theme_id = t.id
                        """,
                (rs, rowNum) -> {
                    ReservationTime time = new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()
                    );
                    Theme theme = new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("description"),
                            rs.getString("url")
                    );
                    return new Reservation(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getDate("date").toLocalDate(),
                            time,
                            theme,
                            ReservationStatus.valueOf(rs.getString("status"))
                    );
                }
        );
    }

    public Optional<Reservation> findById(Long id) {
        List<Reservation> reservations = jdbcTemplate.query(
                """
                            SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                            t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                            r.status
                            FROM reservation r
                            INNER JOIN reservation_time rt ON r.time_id = rt.id
                            INNER JOIN theme t ON r.theme_id = t.id
                            WHERE r.id = ?
                        """,
                (rs, rowNum) -> {
                    ReservationTime time = new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()
                    );
                    Theme theme = new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("description"),
                            rs.getString("url")
                    );
                    return new Reservation(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getDate("date").toLocalDate(),
                            time,
                            theme,
                            ReservationStatus.valueOf(rs.getString("status"))
                    );
                },
                id
        );

        return reservations.stream().findFirst();
    }

    public List<ReservationRank> findByName(String name) {
        return jdbcTemplate.query("""
                            SELECT *
                            FROM (
                                SELECT
                                    r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                                    t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                                    r.status,
                                    CASE WHEN r.status = 'WAITING'
                                         THEN ROW_NUMBER() OVER (PARTITION BY r.date, r.theme_id, r.time_id, r.status ORDER BY r.id)
                                    END AS waiting_order
                                FROM reservation r
                                INNER JOIN reservation_time rt ON r.time_id = rt.id
                                INNER JOIN theme t ON r.theme_id = t.id
                            ) sub
                            WHERE sub.name = ?
                        """,
                (rs, rowNum) -> {
                    ReservationTime time = new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()
                    );
                    Theme theme = new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("description"),
                            rs.getString("url")
                    );
                    Reservation reservation = new Reservation(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getDate("date").toLocalDate(),
                            time,
                            theme,
                            ReservationStatus.valueOf(rs.getString("status"))
                    );

                    return new ReservationRank(reservation, rs.getLong("waiting_order"));
                },
                name
        );
    }

    public boolean existsByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation
                            WHERE date = ?
                                AND time_id = ?
                                AND theme_id = ?
                        )
                        """,
                Boolean.class,
                date,
                time.getId(),
                theme.getId()
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByDateAndThemeAndTimeAndName(LocalDate date, long themeId, long timeId, String name) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation
                            WHERE date = ?
                                AND time_id = ?
                                AND theme_id = ?
                                AND name = ?
                        )
                        """,
                Boolean.class,
                date,
                timeId,
                themeId,
                name
        );
        return Boolean.TRUE.equals(result);
    }

    @Transactional
    public Reservation save(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", reservation.getName());
        params.put("date", reservation.getDate());
        params.put("time_id", reservation.getTime().getId());
        params.put("theme_id", reservation.getTheme().getId());
        params.put("status", reservation.getStatus());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, reservation.getName(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getStatus());
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

}
