package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationWaitingQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("start_at", LocalTime.class)
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );

        return new ReservationWaiting(
                resultSet.getLong("waiting_id"),
                resultSet.getString("waiting_name"),
                resultSet.getObject("waiting_date", LocalDate.class),
                reservationTime,
                theme,
                resultSet.getLong("waiting_sequence"),
                resultSet.getObject("waiting_created_at", LocalDateTime.class)
        );
    };

    public boolean isExistByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, Long timeId, Long themeId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE name = ?
                    AND date = ?
                    AND time_id = ?
                    AND theme_id = ?
            )
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, name, date, timeId, themeId));
    }

    public Optional<ReservationWaiting> findReservationWaitingById(long id) {
        String sql = """
                select
                    w.id,
                    w.name,
                    w.date,
                    w.created_at,
                    t.id       as time_id,
                    t.start_at as time_start_at,
                    th.id          as theme_id,
                    th.name        as theme_name,
                    th.description as theme_description,
                    th.url         as theme_url,
                    ranked.sequence
                from waiting w
                join reservation_time t  ON w.time_id  = t.id
                join theme th            ON w.theme_id = th.id
                join (
                    select
                        id,
                        ROW_NUMBER() OVER (
                            PARTITION BY date, time_id, theme_id
                            ORDER BY created_at, id
                        ) as sequence
                    from waiting
                ) as ranked ON w.id = ranked.id
                where w.id = ?;
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id)
                .stream()
                .findFirst();
    }
}
