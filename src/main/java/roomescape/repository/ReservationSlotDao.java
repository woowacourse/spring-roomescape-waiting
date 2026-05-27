package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.dto.ReservationResponse;

@Repository
public class ReservationSlotDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<ReservationSlot> reservationRowMapper = (rs, rowNum) -> new ReservationSlot(
            rs.getLong("id"),
            rs.getDate("date").toLocalDate(),
            new Time(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))
    );

    public ReservationSlotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_slot")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(LocalDate date, Long timeId, Long themeId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
        )).longValue();
    }

    public ReservationSlot findById(Long id) {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail
                FROM reservation_slot AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                where r.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
    }

    public Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId
    ) {
        String sql = """
                SELECT r.id
                FROM reservation_slot r
                WHERE r.date = ?
                  AND r.time_id = ?
                  AND r.theme_id = ?
                """;

        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                date,
                timeId,
                themeId
        );

        return result.stream().findFirst();
    }

    public List<ReservationSlot> findAll() {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail
                FROM reservation_slot AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }


//    public void updateDateAndTimeById(long id, LocalDate date, long timeId) {
//        jdbcTemplate.update("UPDATE reservationSlot SET date = ?, time_id = ? WHERE id = ?", date, timeId, id);
//    }
//
//    public Optional<ReservationSlot> findByDateAndTimeId(LocalDate date, Long timeId) {
//        String sql = """
//                SELECT r.id AS reservation_id,
//                       r.date AS date,
//                       rt.id AS time_id,
//                       rt.start_at AS start_at,
//                       t.id AS theme_id,
//                       t.name AS theme_name,
//                       t.description AS description,
//                       t.thumbnail_url AS thumbnail_url
//                FROM reservation_slot r
//                JOIN reservation_time rt ON r.time_id = rt.id
//                JOIN theme t ON r.theme_id = t.id
//                WHERE r.date = ? AND r.time_id = ?
//                """;
//        return jdbcTemplate.query(sql, reservationRowMapper, date, timeId)
//                .stream()
//                .findFirst();
//    }

//    public void delete(Long id) {
//        jdbcTemplate.update("DELETE FROM reservationSlot WHERE id = ?", id);
//    }
}
