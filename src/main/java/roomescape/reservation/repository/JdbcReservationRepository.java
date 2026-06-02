package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.dto.PageResult;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservation.repository.exception.ReservationUniqueConstraint;
import roomescape.reservation.repository.exception.RetryableReservationCreateException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static roomescape.reservation.domain.Status.CANCELED;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_EXISTS;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private static final String RESERVATION_COLUMNS = """
                    r.id AS reservation_id,
                    r.guest_name,
                    s.id AS slot_id,
                    s.date,
                    r.status AS status,
                    r.last_modified_at AS last_modified_at,
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
            """;

    private static final String RESERVATION_JOIN = """
                FROM reservation r
                INNER JOIN reservation_slot s
                    ON r.slot_id = s.id
                INNER JOIN reservation_time t
                    ON s.time_id = t.id
                INNER JOIN theme th
                    ON s.theme_id = th.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                """ + RESERVATION_COLUMNS + RESERVATION_JOIN + """
                WHERE r.id = :id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), reservationRowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationWaitingDto> findWaitingById(Long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM (
                            SELECT
                        """ + RESERVATION_COLUMNS + """
                                ,
                                ROW_NUMBER() OVER (
                                    PARTITION BY s.id, r.status
                                    ORDER BY r.last_modified_at
                                ) AS wait_number
                        """ + RESERVATION_JOIN + """
                        ) x
                        WHERE x.reservation_id = :id
                        """,
                new MapSqlParameterSource("id", id), reservationWaitingDtoRowMapper
        ).stream().findFirst();
    }

    @Override
    public PageResult<Reservation> findAllByStatusCanceledNot(int page, int size) {
        List<Reservation> reservations = jdbcTemplate.query("""
                        SELECT
                        """ + RESERVATION_COLUMNS + RESERVATION_JOIN + """
                        WHERE r.status != 'CANCELED'
                        ORDER BY s.date, t.start_at
                        LIMIT :size OFFSET :offset
                        """,
                new MapSqlParameterSource()
                        .addValue("size", size)
                        .addValue("offset", (page - 1) * size),
                reservationRowMapper);
        return PageResult.of(reservations, page, size, countByStatusCanceledNot());
    }

    private long countByStatusCanceledNot() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE status != 'CANCELED'
                """, new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public List<ReservationWaitingDto> findWaitingAllByGuestName(String guestName) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM (
                            SELECT
                        """ + RESERVATION_COLUMNS + """
                                ,
                                ROW_NUMBER() OVER (
                                    PARTITION BY s.id, r.status
                                    ORDER BY r.last_modified_at
                                ) AS wait_number
                        """ + RESERVATION_JOIN + """
                        ) x
                        WHERE x.guest_name = :guestName
                        """,
                new MapSqlParameterSource("guestName", guestName),
                reservationWaitingDtoRowMapper);

    }

    @Override
    public Optional<Reservation> findBySlotAndStatusWaitingAndWaitingNumberIsOne(
            LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.query("""
                                SELECT *
                                FROM (
                                    SELECT
                                """ + RESERVATION_COLUMNS + """
                                        ,
                                        ROW_NUMBER() OVER (
                                            PARTITION BY s.id, r.status
                                            ORDER BY r.last_modified_at
                                        ) AS wait_number
                                """ + RESERVATION_JOIN + """
                                ) x
                                WHERE x.date = :date
                                  AND x.time_id = :timeId
                                  AND x.theme_id = :themeId
                                  AND x.status = 'WAITING'
                                  AND x.wait_number = 1
                                """,
                        new MapSqlParameterSource()
                                .addValue("date", Date.valueOf(date))
                                .addValue("timeId", timeId)
                                .addValue("themeId", themeId),
                        reservationRowMapper)
                .stream().findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        ReservationToken reservationToken = ReservationToken.from(reservation.getStatus());

        try {
            jdbcTemplate.update("""
                            INSERT INTO reservation (
                                guest_name,
                                slot_id,
                                status,
                                last_modified_at,
                                confirm_token,
                                waiting_token
                            )
                            VALUES (:guestName, :slotId, :status, :lastModifiedAt, :confirmToken, :waitingToken)
                            """,
                    new MapSqlParameterSource()
                            .addValue("guestName", reservation.getGuestName())
                            .addValue("slotId", reservation.getReservationSlot().getId())
                            .addValue("status", reservation.getStatus().toString())
                            .addValue("lastModifiedAt", Timestamp.valueOf(reservation.getLastModifiedAt()))
                            .addValue("confirmToken", reservationToken.confirmToken())
                            .addValue("waitingToken", reservationToken.waitingToken()),
                    keyHolder,
                    new String[]{"id"});
        } catch (DuplicateKeyException e) {
            throw convertDuplicateKeyException(e);
        }

        return reservation.withId(keyHolder.getKey().longValue());
    }

    @Override
    public boolean updateSlotAndStatus(Long id, Long slotId, Status status, LocalDateTime lastModifiedAt) {
        ReservationToken reservationToken = ReservationToken.from(status);
        int count;
        try {
            count = jdbcTemplate.update("""
                    UPDATE reservation
                    SET slot_id = :slotId,
                        status = :status,
                        last_modified_at = :lastModifiedAt,
                        confirm_token = :confirmToken,
                        waiting_token = :waitingToken
                    WHERE id = :id
                    """, new MapSqlParameterSource()
                    .addValue("slotId", slotId)
                    .addValue("status", status.toString())
                    .addValue("lastModifiedAt", Timestamp.valueOf(lastModifiedAt))
                    .addValue("id", id)
                    .addValue("confirmToken", reservationToken.confirmToken())
                    .addValue("waitingToken", reservationToken.waitingToken())
            );
        } catch (DuplicateKeyException e) {
            throw convertDuplicateKeyException(e);
        }
        return count == 1;
    }

    private RuntimeException convertDuplicateKeyException(DuplicateKeyException exception) {
        ReservationUniqueConstraint constraint = ReservationUniqueConstraint.from(exception)
                .orElseThrow(() -> new DomainException(GlobalErrorCode.SERVER_ERROR));

        return switch (constraint) {
            case CONFIRMED_SLOT -> new RetryableReservationCreateException();
            case WAITING_GUEST_SLOT -> new DomainException(RESERVATION_ALREADY_EXISTS);
        };
    }

    @Override
    public boolean updateStatus(Long id, Status status) {
        ReservationToken reservationToken = ReservationToken.from(status);
        int count = jdbcTemplate.update("""
                UPDATE reservation
                SET status = :status,
                    confirm_token = :confirmToken,
                    waiting_token = :waitingToken
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("status", status.toString())
                .addValue("id", id)
                .addValue("confirmToken", reservationToken.confirmToken())
                .addValue("waitingToken", reservationToken.waitingToken())
        );
        return count == 1;
    }

    @Override
    public boolean cancelById(Long id) {
        ReservationToken reservationToken = ReservationToken.from(CANCELED);
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET status = 'CANCELED',
                    confirm_token = :confirmToken,
                    waiting_token = :waitingToken
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("confirmToken", reservationToken.confirmToken())
                .addValue("waitingToken", reservationToken.waitingToken())
        );

        return rowCount == 1;
    }

    @Override
    public boolean existsBySlotAndGuestNameExceptCanceled(ReservationSlot slot, String guestName) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE slot_id = :slotId
                          AND guest_name = :guestName
                          AND status != 'CANCELED'
                        """,
                new MapSqlParameterSource()
                        .addValue("slotId", slot.getId())
                        .addValue("guestName", guestName),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySlotAndStatusConfirmed(ReservationSlot reservationSlot) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE slot_id = :slotId
                          AND status = 'CONFIRMED'
                        """,
                new MapSqlParameterSource()
                        .addValue("slotId", reservationSlot.getId()),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation r
                INNER JOIN reservation_slot s
                    ON r.slot_id = s.id
                WHERE s.time_id = :timeId
                  AND r.status != 'CANCELED'
                """, new MapSqlParameterSource("timeId", timeId), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation r
                INNER JOIN reservation_slot s
                    ON r.slot_id = s.id
                WHERE s.theme_id = :themeId
                  AND r.status != 'CANCELED'
                """, new MapSqlParameterSource("themeId", themeId), Integer.class);
        return count != null && count > 0;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime(),
                toLocalDateTime(resultSet.getTimestamp("time_deleted_at"))
        );

        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail"),
                toLocalDateTime(resultSet.getTimestamp("theme_deleted_at"))
        );

        ReservationSlot reservationSlot = ReservationSlot.of(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("guest_name"),
                reservationSlot,
                Status.from(resultSet.getString("status")),
                toLocalDateTime(resultSet.getTimestamp("last_modified_at"))
        );
    };

    private final RowMapper<ReservationWaitingDto> reservationWaitingDtoRowMapper = (resultSet, rowNum) ->
            ReservationWaitingDto.from(reservationRowMapper.mapRow(resultSet, rowNum), resultSet.getLong("wait_number"));

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
