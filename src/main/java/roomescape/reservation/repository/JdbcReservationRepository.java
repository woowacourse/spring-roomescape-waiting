package roomescape.reservation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InfrastructureException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
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

        return new Reservation(
                resultSet.getLong("reservation_id"),
                ReservationStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("waiting_rank", Long.class),
                resultSet.getString("name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = selectReservations("", "");

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findByName(String name) {
        String historyUnionClause = """
                UNION ALL
                SELECT h.reservation_id,
                       h.name,
                       h.date,
                       h.time_id,
                       rt.start_at,
                       h.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       'CANCELED' AS status,
                       CAST(NULL AS BIGINT) AS waiting_rank,
                       h.request_order,
                       1 AS source_order
                FROM reservation_history h
                JOIN reservation_time rt ON h.time_id = rt.id
                JOIN theme t ON h.theme_id = t.id
                WHERE h.name = ?
                """;
        String sql = selectReservations("WHERE r.name = ?", historyUnionClause);

        return jdbcTemplate.query(sql, reservationRowMapper, name, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = selectReservations("WHERE r.id = ?", "");

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = selectReservations("WHERE r.date = ? AND r.theme_id = ?", "");

        return jdbcTemplate.query(sql, reservationRowMapper, date, themeId);
    }

    private String selectReservations(String whereClause, String additionalRowsClause) {
        return """
                SELECT result.reservation_id,
                       result.name,
                       result.date,
                       result.time_id,
                       result.start_at,
                       result.theme_id,
                       result.theme_name,
                       result.theme_description,
                       result.theme_thumbnail,
                       result.status,
                       result.waiting_rank
                FROM (
                    SELECT ranked.reservation_id,
                           ranked.name,
                           ranked.date,
                           ranked.time_id,
                           ranked.start_at,
                           ranked.theme_id,
                           ranked.theme_name,
                           ranked.theme_description,
                           ranked.theme_thumbnail,
                           CASE
                               WHEN ranked.queue_position = 1 THEN 'RESERVED'
                               ELSE 'WAITING'
                           END AS status,
                           ranked.queue_position - 1 AS waiting_rank,
                           ranked.request_order,
                           0 AS source_order
                    FROM (
                        SELECT r.id AS reservation_id,
                               r.name,
                               r.date,
                               r.time_id,
                               rt.start_at,
                               r.theme_id,
                               t.name AS theme_name,
                               t.description AS theme_description,
                               t.thumbnail AS theme_thumbnail,
                               r.request_order,
                               (
                                   SELECT COUNT(*)
                                   FROM reservation same_slot
                                   WHERE same_slot.date = r.date
                                     AND same_slot.time_id = r.time_id
                                     AND same_slot.theme_id = r.theme_id
                                     AND same_slot.request_order <= r.request_order
                               ) AS queue_position
                        FROM reservation r
                        JOIN reservation_time rt ON r.time_id = rt.id
                        JOIN theme t ON r.theme_id = t.id
                        %s
                    ) ranked
                    %s
                ) result
                ORDER BY result.date ASC,
                         result.start_at ASC,
                         result.request_order ASC,
                         result.source_order ASC
                """.formatted(whereClause, additionalRowsClause);
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(reservation, keyHolder);
        validateCreatedRowCount(rowCount, reservation);

        Long id = getGeneratedId(keyHolder, reservation);
        return findById(id)
                .orElseThrow(() -> new InfrastructureException("예약 생성 결과를 조회하지 못했습니다."));
    }

    @Override
    @Transactional
    public Reservation updateDateTime(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET date = ?,
                    time_id = ?,
                    request_order = NEXT VALUE FOR reservation_request_order_seq
                WHERE id = ?
                """;

        int updatedRowCount = jdbcTemplate.update(
                sql,
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getId()
        );

        validateUpdatedRowCount(updatedRowCount, reservation);

        return findById(reservation.getId())
                .orElseThrow(() -> new InfrastructureException("예약 변경 결과를 조회하지 못했습니다."));
    }

    @Override
    @Transactional
    public Reservation moveToHistory(Reservation reservation) {
        int insertedRowCount = insertHistory(reservation);
        validateUpdatedRowCount(insertedRowCount, reservation);

        int deletedRowCount = delete(reservation.getId());
        validateUpdatedRowCount(deletedRowCount, reservation);

        return reservation;
    }

    private int insertHistory(Reservation reservation) {
        String sql = """
                INSERT INTO reservation_history (
                    reservation_id,
                    name,
                    date,
                    time_id,
                    theme_id,
                    request_order,
                    created_at,
                    canceled_at
                )
                SELECT id,
                       name,
                       date,
                       time_id,
                       theme_id,
                       request_order,
                       created_at,
                       CURRENT_TIMESTAMP
                FROM reservation
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, reservation.getId());
    }

    private int insert(Reservation reservation, KeyHolder keyHolder) {
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
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());
            return preparedStatement;
        }, keyHolder);
    }

    private void validateCreatedRowCount(int rowCount, Reservation reservation) {
        if (rowCount != 1) {
            log.error(
                    "Reservation insert affected unexpected row count. rowCount={}, name={}, date={}, timeId={}, themeId={}",
                    rowCount,
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    reservation.getTheme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }
    }

    private void validateUpdatedRowCount(int rowCount, Reservation reservation) {
        if (rowCount != 1) {
            log.error(
                    "Reservation update affected unexpected row count. rowCount={}, id={}, name={}, date={}, timeId={}, themeId={}, status={}",
                    rowCount,
                    reservation.getId(),
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    reservation.getTheme().getId(),
                    reservation.getStatus()
            );
            throw new InfrastructureException("예약 변경에 실패했습니다.");
        }
    }

    private Long getGeneratedId(KeyHolder keyHolder, Reservation reservation) {
        Number key = keyHolder.getKey();
        if (key == null) {
            log.error(
                    "Reservation insert did not return generated id. name={}, date={}, timeId={}, themeId={}",
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    reservation.getTheme().getId()
            );
            throw new InfrastructureException("예약 생성에 실패했습니다.");
        }
        return key.longValue();
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
        delete(id);
    }

    private int delete(Long id) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, id);
    }
}
