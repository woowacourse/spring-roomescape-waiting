package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingSequence;

@Repository
public class ReservationWaitingQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingQueryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_RESERVATION_WAITING_SQL = """
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
                """;

    private final RowMapper<ReservationWaitingSequence> reservationWaitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_start_at", LocalTime.class)
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );

        ReservationSlot slot = new ReservationSlot(
                resultSet.getObject("date", LocalDate.class),
                reservationTime,
                theme
        );

        ReservationWaiting reservationWaiting = new ReservationWaiting(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                slot,
                resultSet.getObject("created_at", LocalDateTime.class)
        );

        return new ReservationWaitingSequence(reservationWaiting, resultSet.getLong("sequence"));
    };

    public boolean isExistByNameAndSlot(String name, ReservationSlot slot) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM waiting
                    WHERE name = ?
                    AND date = ?
                    AND time_id = ?
                    AND theme_id = ?
            )
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, name, slot.getDate(), slot.getTimeId(), slot.getThemeId()));
    }

    public Optional<ReservationWaitingSequence> findReservationWaitingById(long id) {
        String sql = SELECT_RESERVATION_WAITING_SQL + "where w.id = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id)
                .stream()
                .findFirst();
    }

    public List<ReservationWaitingSequence> findAllReservationWaiting() {
        return jdbcTemplate.query(SELECT_RESERVATION_WAITING_SQL, reservationWaitingRowMapper);
    }

    public List<ReservationWaitingSequence> findAllByName(String name) {
        String sql = SELECT_RESERVATION_WAITING_SQL + "where w.name = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, name);
    }

    public Optional<ReservationWaiting> findFirstWaitingBySlot(ReservationSlot slot) {
        String sql = SELECT_RESERVATION_WAITING_SQL + "where w.date = ? and w.time_id = ? and w.theme_id = ? and ranked.sequence = 1";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, slot.getDate(), slot.getTimeId(), slot.getThemeId())
                .stream()
                .findFirst()
                .map(ReservationWaitingSequence::reservationWaiting);
    }
}
