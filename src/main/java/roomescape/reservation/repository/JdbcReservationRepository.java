package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("guest_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                Status.from(resultSet.getString("status"))
        );
    };
    private final RowMapper<ReservationWaitingResult> reservationWaitingDtoRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );

        return ReservationWaitingResult.from(Reservation.of(
                        resultSet.getLong("reservation_id"),
                        resultSet.getString("guest_name"),
                        resultSet.getDate("date").toLocalDate(),
                        reservationTime,
                        theme,
                        Status.from(resultSet.getString("status"))),
                resultSet.getLong("wait_number")
        );
    };

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationWaitingResult> findWaitingById(Long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM (
                            SELECT
                                r.id AS reservation_id,
                                r.guest_name,
                                r.date,
                                r.status AS status,
                        
                                t.id AS time_id,
                                t.start_at,
                        
                                th.id AS theme_id,
                                th.name AS theme_name,
                                th.description AS theme_description,
                                th.thumbnail AS theme_thumbnail,
                        
                                ROW_NUMBER() OVER (
                                    PARTITION BY r.date, t.id, th.id, r.status
                                    ORDER BY r.created_at, r.id
                                ) AS wait_number
                        
                            FROM reservation r
                            INNER JOIN reservation_time t
                                ON r.time_id = t.id
                            INNER JOIN theme th
                                ON r.theme_id = th.id
                        ) x
                        WHERE x.reservation_id = ?
                        """,
                reservationWaitingDtoRowMapper,
                id
        ).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                ORDER BY r.id
                LIMIT ? OFFSET ?
                """, reservationRowMapper, size, (page - 1) * size);
    }

    @Override
    public List<ReservationWaitingResult> findAllByGuestName(String guestName) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                
                    t.id AS time_id,
                    t.start_at,
                
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, t.id, th.id, r.status
                        ORDER BY r.created_at, r.id
                    ) AS wait_number
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.guest_name = ?
                """, reservationWaitingDtoRowMapper, guestName);
    }

    @Override
    public List<ReservationWaitingResult> findAllByGuestNameExceptCanceled(String guestName) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                
                    t.id AS time_id,
                    t.start_at,
                
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, t.id, th.id, r.status
                        ORDER BY r.created_at, r.id
                    ) AS wait_number
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.guest_name = ? AND r.status != ?
                """, reservationWaitingDtoRowMapper, guestName, Status.CANCELED.toString());
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation (guest_name, date, time_id, theme_id, status, confirmed_token)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservation.getGuestName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getSlot().date()));
            preparedStatement.setLong(3, reservation.getSlot().timeId());
            preparedStatement.setLong(4, reservation.getSlot().themeId());
            preparedStatement.setString(5, reservation.getStatus().toString());
            preparedStatement.setObject(6, toConfirmedToken(reservation.getStatus()));
            return preparedStatement;
        }, keyHolder);

        return reservation.withId(keyHolder.getKey().longValue());
    }

    @Override
    public boolean updateSlot(Long id, ReservationSlot slot, Status status) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?, status = ?, confirmed_token = ?
                WHERE id = ?
                """;

        int count = jdbcTemplate.update(sql,
                slot.date(),
                slot.timeId(),
                status.toString(),
                toConfirmedToken(status),
                id);

        return count == 1;
    }

    @Override
    public boolean cancelById(Long id) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET status = ?, confirmed_token = NULL
                WHERE id = ?
                """, Status.CANCELED.toString(), id);

        return rowCount == 1;
    }

    @Override
    public boolean updateStatus(Long id, Status status) {
        int rowCount = jdbcTemplate.update("""
            UPDATE reservation
            SET status = ?, confirmed_token = ?
            WHERE id = ?
              AND status = ?
            """,
                status.toString(),
                toConfirmedToken(status),
                id,
                Status.WAITING.toString()
        );
        return rowCount == 1;
    }

    @Override
    public Optional<Long> findFirstWaitingIdBySlot(ReservationSlot slot) {
        return jdbcTemplate.query("""
                        SELECT id
                        FROM reservation
                        WHERE date = ? AND time_id = ? AND theme_id = ? AND status = ?
                        ORDER BY created_at, id
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                slot.date(),
                slot.timeId(),
                slot.themeId(),
                Status.WAITING.toString()
        ).stream().findFirst();
    }

    @Override
    public boolean existsBySlotAndGuestNameExceptCanceled(ReservationSlot slot, String guestName) {
        return existsReservation("""
                        date = ? AND time_id = ? AND theme_id = ? AND guest_name = ? AND status != ?
                        """,
                slot.date(), slot.timeId(), slot.themeId(), guestName,
                Status.CANCELED.toString()
        );
    }

    @Override
    public boolean existsReservationBySlot(ReservationSlot slot) {
        return existsReservation("""
                        date = ? AND time_id = ? AND theme_id = ? AND status = ?
                        """,
                slot.date(), slot.timeId(), slot.themeId(),
                Status.CONFIRMED.toString()
        );
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        return existsReservation("""
                        time_id = ? AND status != ?
                        """,
                timeId,
                Status.CANCELED.toString()
        );
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        return existsReservation("""
                        theme_id = ? AND status != ?
                        """,
                themeId,
                Status.CANCELED.toString()
        );
    }

    private boolean existsReservation(String condition, Object... args) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE
                """ + condition + """
                )
                """, Boolean.class, args);
        return Boolean.TRUE.equals(exists);
    }

    private Integer toConfirmedToken(Status status) {
        if (status == Status.CONFIRMED) {
            return 1;
        }
        return null;
    }

}
