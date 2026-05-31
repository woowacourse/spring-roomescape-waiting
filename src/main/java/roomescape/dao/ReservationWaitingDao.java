package roomescape.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.response.ReservationWaitingOrderResponse;

@Repository
public class ReservationWaitingDao {
    private static final RowMapper<ReservationWaiting> ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );

        return new ReservationWaiting(
                resultSet.getLong("id"),
                resultSet.getString("waiting_name"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationWaiting insert(ReservationWaiting reservationWaiting) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservationWaiting.getName());
        parameters.put("created_at", reservationWaiting.getCreatedAt());
        parameters.put("reservation_date", reservationWaiting.getReservationDate());
        parameters.put("time_id", reservationWaiting.getTime().getId());
        parameters.put("theme_id", reservationWaiting.getTheme().getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new ReservationWaiting(
                generatedId.longValue(),
                reservationWaiting.getName(),
                reservationWaiting.getCreatedAt(),
                reservationWaiting.getReservationDate(),
                reservationWaiting.getTime(),
                reservationWaiting.getTheme()
        );
    }

    public boolean existsByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation_waiting
                WHERE name = ?
                AND reservation_date = ?
                AND time_id = ? 
                AND theme_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name, date, timeId, themeId);
    }

    public int delete(Long reservationWaitingId) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?";
        return jdbcTemplate.update(sql, reservationWaitingId);
    }

    public int countOrder(LocalDate reservationDate, long timeId, long themeId, long waitingId) {
        String sql = """
                SELECT COUNT(*) 
                FROM reservation_waiting 
                WHERE reservation_date = ?
                AND time_id = ?
                AND theme_id = ?
                AND id <= ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, reservationDate, timeId, themeId, waitingId);
    }

    public List<ReservationWaiting> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public List<ReservationWaitingOrderResponse> selectByNameWithOrder(String name) {
        String sql = """
                 SELECT rw.id,
                       rw.name as waiting_name,
                       rw.created_at,
                       rw.reservation_date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail,
                       ROW_NUMBER() OVER (
                           PARTITION BY rw.reservation_date, rw.time_id, rw.theme_id
                           ORDER BY rw.id
                       ) as order_num
                FROM reservation_waiting as rw
                INNER JOIN reservation_time as rt ON rw.time_id = rt.id
                INNER JOIN theme as t ON rw.theme_id = t.id
                WHERE rw.name = ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            ReservationWaiting waiting = ROW_MAPPER.mapRow(resultSet, rowNum);
            int order = resultSet.getInt("order_num");
            return new ReservationWaitingOrderResponse(waiting, order);
        }, name);
    }

    private String baseSelectSql() {
        return """
                SELECT rw.id, 
                       rw.name as waiting_name, 
                       rw.created_at, 
                       rw.reservation_date, 
                       rt.id as time_id, 
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name, 
                       t.description,
                       t.thumbnail
                FROM reservation_waiting as rw
                INNER JOIN reservation_time as rt 
                ON rw.time_id = rt.id
                INNER JOIN theme as t 
                ON rw.theme_id = t.id 
                """;
    }
}
