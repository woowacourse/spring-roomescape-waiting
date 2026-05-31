package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationWaitingDao {
    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_RESERVATION_WAITING_SQL = """
            SELECT
                w.id,
                w.name,
                w.created_at,
                r.id         as reservation_id,
                r.name       as reservation_name,
                r.date       as reservation_date,
                r.created_at as reservation_created_at,
                t.id         as time_id,
                t.start_at   as time_start_at,
                th.id          as theme_id,
                th.name        as theme_name,
                th.description as theme_description,
                th.url         as theme_url,
                ranked.sequence
            FROM waiting w
            JOIN reservation r ON w.reservation_id = r.id
            JOIN reservation_time t  ON r.time_id  = t.id
            JOIN theme th            ON r.theme_id = th.id
            JOIN (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        PARTITION BY reservation_id
                        ORDER BY created_at, id
                    ) as sequence
                FROM waiting
            ) as ranked ON w.id = ranked.id
            """;

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (resultSet, rowNum) -> {
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

        Reservation reservation = Reservation.restore(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getObject("reservation_date", LocalDate.class),
                reservationTime,
                theme,
                resultSet.getObject("reservation_created_at", LocalDateTime.class)
        );

        return ReservationWaiting.restore(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                reservation,
                resultSet.getLong("sequence"),
                resultSet.getObject("created_at", LocalDateTime.class)
        );
    };

    public boolean isExistByNameAndReservationId(String name, Long reservationId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM waiting
                    WHERE name = ?
                    AND reservation_id = ?
            )
            """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name, reservationId);
    }

    public Optional<ReservationWaiting> findReservationWaitingById(long id) {
        String sql = SELECT_RESERVATION_WAITING_SQL + "where w.id = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id)
                .stream()
                .findFirst();
    }

    public Optional<ReservationWaiting> findSharedFirstByReservationId(Long reservationId) {
        String sql = """
                select for share
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
                WHERE w.reservation_id = ?
                ORDER BY w.created_at, w.id
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, reservationWaitingRowMapper, reservationId)
                .stream()
                .findFirst();
    }

    public List<ReservationWaiting> findAllReservationWaiting() {
        return jdbcTemplate.query(SELECT_RESERVATION_WAITING_SQL, reservationWaitingRowMapper);
    }

    public List<ReservationWaiting> findAllByName(String name) {
        String sql = SELECT_RESERVATION_WAITING_SQL + "where w.name = ?";
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, name);
    }

    public Long create(ReservationWaiting reservationWaiting) {
        String sql = "insert into waiting(name, reservation_id, created_at) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setLong(2, reservationWaiting.getReservation().getId());
            ps.setObject(3, reservationWaiting.getCreatedAt());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void delete(Long id) {
        String sql = "delete from waiting where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
