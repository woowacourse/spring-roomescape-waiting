package roomescape.reservationslot.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.domain.exception.ReservationSlotNotFoundException;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationslot.repository.entity.ReservationSlotEntity;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private static final RowMapper<ReservationSlot> RESERVATION_SLOT_ROW_MAPPER = (rs, rowNum) -> mapToDomain(rs);
    private static final RowMapper<Waiting> WAITING_ROW_MAPPER = (rs, rowNum) -> {
        final ReservationTime reservationTime = ReservationTime.of(
                rs.getLong("time_id"),
                rs.getTime("time_start_at").toLocalTime()
        );
        final Theme theme = Theme.of(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail_url")
        );
        final ReservationSlot slot = ReservationSlot.of(
                rs.getLong("slot_id"),
                rs.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );

        return Waiting.of(
                rs.getLong("waiting_id"),
                rs.getString("waiting_customer_name"),
                slot,
                rs.getTimestamp("waiting_created_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReservationSlot findOrCreate(final java.time.LocalDate date, final ReservationTime time, final Theme theme) {
        return findByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())
                .orElseGet(() -> saveOrFind(date, time, theme));
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(
            final java.time.LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        final String sql = """
                SELECT
                    s.id AS slot_id,
                    s.reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation_slot s
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                WHERE s.reservation_date = ? AND s.time_id = ? AND s.theme_id = ?
                """;

        return findByDateAndTimeIdAndThemeId(date, timeId, themeId, sql);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeIdForUpdate(
            final java.time.LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        final String sql = """
                SELECT
                    s.id AS slot_id,
                    s.reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation_slot s
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                WHERE s.reservation_date = ? AND s.time_id = ? AND s.theme_id = ?
                FOR UPDATE
                """;

        return findByDateAndTimeIdAndThemeId(date, timeId, themeId, sql);
    }

    private Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(
            final java.time.LocalDate date,
            final Long timeId,
            final Long themeId,
            final String sql
    ) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    RESERVATION_SLOT_ROW_MAPPER,
                    Date.valueOf(date),
                    timeId,
                    themeId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void deleteReservationAndPromoteWaiting(final Reservation reservation) {
        final ReservationSlot slot = findByIdForUpdate(reservation.getSlotId())
                .orElseThrow(ReservationSlotNotFoundException::new);

        if (!deleteCurrentReservation(reservation.getId(), slot.getId())) {
            throw new ReservationNotFoundException();
        }

        findEarliestWaitingBySlotId(slot.getId())
                .ifPresent(this::promoteWaiting);

        deleteSlotIfEmpty(slot.getId());
    }

    private ReservationSlot saveOrFind(final java.time.LocalDate date, final ReservationTime time, final Theme theme) {
        try {
            return save(ReservationSlot.create(date, time, theme));
        } catch (DuplicateKeyException exception) {
            return findByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())
                    .orElseThrow(() -> exception);
        }
    }

    private ReservationSlot save(final ReservationSlot slot) {
        final ReservationSlotEntity slotEntity = toEntity(slot);
        final String sql = """
                INSERT INTO reservation_slot (reservation_date, time_id, theme_id)
                VALUES (?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setDate(1, slotEntity.date());
            preparedStatement.setLong(2, slotEntity.timeId());
            preparedStatement.setLong(3, slotEntity.themeId());

            return preparedStatement;
        }, keyHolder);

        return ReservationSlot.of(
                generatedIdFrom(keyHolder),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme()
        );
    }

    private Optional<ReservationSlot> findByIdForUpdate(final Long slotId) {
        final String sql = """
                SELECT
                    s.id AS slot_id,
                    s.reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation_slot s
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                WHERE s.id = ?
                FOR UPDATE
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, RESERVATION_SLOT_ROW_MAPPER, slotId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private boolean deleteCurrentReservation(final Long reservationId, final Long slotId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = ? AND slot_id = ?
                """;

        return jdbcTemplate.update(sql, reservationId, slotId) > 0;
    }

    private Optional<Waiting> findEarliestWaitingBySlotId(final Long slotId) {
        final String sql = """
                SELECT
                    w.id AS waiting_id,
                    w.customer_name AS waiting_customer_name,
                    w.created_at AS waiting_created_at,
                    s.id AS slot_id,
                    s.reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM waiting w
                JOIN reservation_slot s ON w.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                WHERE w.slot_id = ?
                ORDER BY w.created_at ASC, w.id ASC
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, WAITING_ROW_MAPPER, slotId)
                .stream()
                .findFirst();
    }

    private void promoteWaiting(final Waiting waiting) {
        final String reservationSql = """
                INSERT INTO reservation (customer_name, slot_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(
                reservationSql,
                waiting.getCustomerName().name(),
                waiting.getSlotId()
        );

        deleteWaitingById(waiting.getId());
    }

    private boolean deleteWaitingById(final long waitingId) {
        final String sql = """
                DELETE FROM waiting
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, waitingId) > 0;
    }

    private boolean deleteSlotIfEmpty(final Long slotId) {
        final String sql = """
                DELETE FROM reservation_slot
                WHERE id = ?
                  AND NOT EXISTS (SELECT 1 FROM reservation WHERE slot_id = ?)
                  AND NOT EXISTS (SELECT 1 FROM waiting WHERE slot_id = ?)
                """;

        return jdbcTemplate.update(sql, slotId, slotId, slotId) > 0;
    }

    private static ReservationSlot mapToDomain(final ResultSet resultSet) throws SQLException {
        final ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        final Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        return ReservationSlot.of(
                resultSet.getLong("slot_id"),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );
    }

    private ReservationSlotEntity toEntity(final ReservationSlot slot) {
        return new ReservationSlotEntity(
                slot.getId(),
                Date.valueOf(slot.getDate()),
                slot.getTimeId(),
                slot.getThemeId()
        );
    }

    private long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성된 예약 슬롯 id를 가져오지 못했습니다.");
        }

        return keyHolder.getKey().longValue();
    }
}
