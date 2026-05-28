package roomescape.dao;

import static roomescape.dao.rowMapper.ReservationMapper.RESERVATION_ROW_MAPPER;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;

@Repository
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
                            t.id AS theme_id, t.name AS theme_name, t.description, t.url
                            FROM reservation r
                            INNER JOIN reservation_time rt ON r.time_id = rt.id
                            INNER JOIN theme t ON r.theme_id = t.id;
                        """,
                RESERVATION_ROW_MAPPER
        );
    }

    public List<Reservation> findAllByUserName(String userName) {
        String sql = """
                SELECT r.id, r.name,r.date,rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.date, rt.start_at ASC;
                """;
        return jdbcTemplate.query(
                sql,
                RESERVATION_ROW_MAPPER,
                userName
        );
    }

    public boolean existsBy(LocalDate date, Theme theme, ReservationTime time) {
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

    public Reservation save(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", reservation.getName().value());
        params.put("date", reservation.getDate());
        params.put("time_id", reservation.getTime().getId());
        params.put("theme_id", reservation.getTheme().getId());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());
    }

    public boolean update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?, date = ?, time_id = ?, theme_id = ?
                WHERE id = ?;
                """;

        int affectedRows = jdbcTemplate.update(
                sql,
                reservation.getName().value(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );

        return affectedRows > 0;
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE id = ?
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                id
        );

        return Boolean.TRUE.equals(result);
    }

    public boolean existsByUserNameAndSlot(String userName, LocalDate date, Theme theme, ReservationTime time) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE name = ?
                        AND date = ?
                        AND time_id = ?
                        AND theme_id = ?
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                userName,
                date,
                time.getId(),
                theme.getId()
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                timeId
        );
        return Boolean.TRUE.equals(result);
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id, r.name,r.date,rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        RESERVATION_ROW_MAPPER,
                        id
                ).stream()
                .findFirst();
    }
}
