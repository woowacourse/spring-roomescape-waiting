package roomescape.reservation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.InfrastructureException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReservationRepository.class);

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );
        Slot reservationSlot = new Slot(
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );

        return Reservation.reconstruct(
                resultSet.getLong("reservation_id"),
                resultSet.getString("name"),
                reservationSlot
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name,
                       r.date,
                       r.time_id,
                       rt.start_at,
                       r.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       r.request_order
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                ORDER BY r.date ASC,
                         rt.start_at ASC,
                         r.request_order ASC,
                         r.id ASC
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findAllForName(String name) {
        String sql = """
                WITH target_schedules AS (
                    SELECT DISTINCT date, time_id, theme_id
                    FROM reservation
                    WHERE name = ?
                )
                SELECT r.id AS reservation_id,
                       r.name,
                       r.date,
                       r.time_id,
                       rt.start_at,
                       r.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       r.request_order
                FROM reservation r
                JOIN target_schedules target
                    ON target.date = r.date
                   AND target.time_id = r.time_id
                   AND target.theme_id = r.theme_id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                ORDER BY r.date ASC,
                         rt.start_at ASC,
                         r.request_order ASC,
                         r.id ASC
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name,
                       r.date,
                       r.time_id,
                       rt.start_at,
                       r.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       r.request_order
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name,
                       r.date,
                       r.time_id,
                       rt.start_at,
                       r.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       r.request_order
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.date = ? AND r.theme_id = ?
                ORDER BY r.date ASC,
                         rt.start_at ASC,
                         r.request_order ASC,
                         r.id ASC
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, date, themeId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(reservation, keyHolder);
        validateCreatedRowCount(rowCount, reservation);

        Long id = getGeneratedId(keyHolder, reservation);
        return reservation.withId(id);
    }

    @Override
    public Optional<Reservation> updateDateTime(Reservation reservation) {
        Slot reservationSlot = reservation.getSlot();
        String sql = """
                UPDATE reservation
                SET date = ?,
                    time_id = ?,
                    request_order = NEXT VALUE FOR reservation_request_order_seq
                WHERE id = ?
                """;

        int rowCount = jdbcTemplate.update(
                sql,
                Date.valueOf(reservationSlot.date()),
                reservationSlot.time().getId(),
                reservation.getId()
        );

        return toUpdatedReservation(rowCount, reservation);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, id);
    }

    private int insert(Reservation reservation, KeyHolder keyHolder) {
        Slot reservationSlot = reservation.getSlot();
        String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id, request_order, created_at)
                VALUES (?, ?, ?, ?, NEXT VALUE FOR reservation_request_order_seq, CURRENT_TIMESTAMP)
                """;

        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservation.getName());
            preparedStatement.setDate(2, Date.valueOf(reservationSlot.date()));
            preparedStatement.setLong(3, reservationSlot.time().getId());
            preparedStatement.setLong(4, reservationSlot.theme().getId());
            return preparedStatement;
        }, keyHolder);
    }

    private void validateCreatedRowCount(int rowCount, Reservation reservation) {
        if (rowCount != 1) {
            Slot reservationSlot = reservation.getSlot();
            log.error(
                    "Reservation insert affected unexpected row count. rowCount={}, name={}, date={}, timeId={}, themeId={}",
                    rowCount,
                    reservation.getName(),
                    reservationSlot.date(),
                    reservationSlot.time().getId(),
                    reservationSlot.theme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }
    }

    private Optional<Reservation> toUpdatedReservation(int rowCount, Reservation reservation) {
        if (rowCount == 0) {
            return Optional.empty();
        }
        if (rowCount != 1) {
            Slot reservationSlot = reservation.getSlot();
            log.error(
                    "Reservation update affected unexpected row count. rowCount={}, id={}, name={}, date={}, timeId={}, themeId={}",
                    rowCount,
                    reservation.getId(),
                    reservation.getName(),
                    reservationSlot.date(),
                    reservationSlot.time().getId(),
                    reservationSlot.theme().getId()
            );
            throw new InfrastructureException("예약 변경에 실패했습니다.");
        }

        return Optional.of(reservation);
    }

    private Long getGeneratedId(KeyHolder keyHolder, Reservation reservation) {
        Number key = keyHolder.getKey();
        if (key == null) {
            Slot reservationSlot = reservation.getSlot();
            log.error(
                    "Reservation insert did not return generated id. name={}, date={}, timeId={}, themeId={}",
                    reservation.getName(),
                    reservationSlot.date(),
                    reservationSlot.time().getId(),
                    reservationSlot.theme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }

        return key.longValue();
    }
}
