package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.domain.PendingReservationRepository;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcPendingReservationRepository implements PendingReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<PendingReservation> rowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.builder()
                .id(resultSet.getLong("t_id"))
                .name(resultSet.getString("t_name"))
                .thumbnailImageUrl(resultSet.getString("t_thumbnail_image_url"))
                .description(resultSet.getString("t_description"))
                .durationTime(resultSet.getTime("t_duration_time").toLocalTime())
                .build();

        ReservationTime time = ReservationTime.builder()
                .id(resultSet.getLong("rt_id"))
                .startAt(resultSet.getTime("rt_start_at").toLocalTime())
                .build();

        TimeSlot slot = TimeSlot.builder()
                .id(resultSet.getLong("ts_id"))
                .date(resultSet.getDate("ts_date").toLocalDate())
                .time(time)
                .theme(theme)
                .build();

        return PendingReservation.builder()
                .id(resultSet.getLong("p_id"))
                .name(resultSet.getString("p_name"))
                .slot(slot)
                .is_deleted(resultSet.getLong("p_is_deleted"))
                .createdAt(resultSet.getTimestamp("p_created_at").toLocalDateTime())
                .build();
    };
    private final RowMapper<ReservationQueryResult> queryResultRowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.builder()
                .id(resultSet.getLong("t_id"))
                .name(resultSet.getString("t_name"))
                .thumbnailImageUrl(resultSet.getString("t_thumbnail_image_url"))
                .description(resultSet.getString("t_description"))
                .durationTime(resultSet.getTime("t_duration_time").toLocalTime())
                .build();

        ReservationTime time = ReservationTime.builder()
                .id(resultSet.getLong("rt_id"))
                .startAt(resultSet.getTime("rt_start_at").toLocalTime())
                .build();

        TimeSlot slot = TimeSlot.builder()
                .id(resultSet.getLong("ts_id"))
                .date(resultSet.getDate("ts_date").toLocalDate())
                .time(time)
                .theme(theme)
                .build();

        return ReservationQueryResult.builder()
                .id(resultSet.getLong("p_id"))
                .name(resultSet.getString("p_name"))
                .slot(slot)
                .pendingIndex(resultSet.getObject("pending_index", Long.class))
                .createdAt(resultSet.getTimestamp("p_created_at").toLocalDateTime())
                .build();
    };

    @Override
    public boolean existsReservationByName(final Long slotId, final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM pending WHERE slot_id=:slotId AND name=:name AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("slotId", slotId, "name", name),
                        Boolean.class));
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM pending WHERE id = :id AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("id", id),
                        Boolean.class));
    }

    @Override
    public int cancel(PendingReservation reservation) {
        String sql = "UPDATE pending SET is_deleted=:id WHERE id = :id AND is_deleted = 0";
        return jdbcTemplate.update(sql, Map.of("id", reservation.getId()));
    }

    @Override
    public int update(PendingReservation reservation) {
        String sql = "UPDATE pending "
                + "SET slot_id=:slotId, created_at = :createdAt "
                + "WHERE id = :id AND is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("id", reservation.getId())
                .addValue("createdAt", reservation.getCreatedAt());
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public List<PendingReservation> findAll() {
        String sql = "SELECT "
                + "p.id AS p_id, p.name AS p_name, p.is_deleted AS p_is_deleted, p.created_at AS p_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "INNER JOIN time_slot ts ON p.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE p.is_deleted = 0 "
                + "ORDER BY ts.date ASC, rt.start_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<ReservationQueryResult> findAllByName(String name) {
        String sql = "SELECT "
                + "p.id AS p_id, p.name AS p_name, p.created_at AS p_created_at, "
                + "(SELECT COUNT(*) FROM pending p2 "
                + " WHERE p2.slot_id = p.slot_id "
                + " AND p2.is_deleted = 0 "
                + " AND p2.created_at <= p.created_at) AS pending_index, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "INNER JOIN time_slot ts ON p.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE p.name = :name AND p.is_deleted = 0 "
                + "ORDER BY ts.date ASC, rt.start_at ASC";
        return jdbcTemplate.query(sql, Map.of("name", name), queryResultRowMapper);
    }

    @Override
    public PendingReservation save(PendingReservation reservation) {
        String sql = "INSERT INTO pending(name, slot_id, created_at) "
                + "VALUES(:name, :slotId, :createdAt)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("createdAt", reservation.getCreatedAt());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return reservation.withId(generatedId);
    }

    @Override
    public PendingReservation insertWithId(PendingReservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.getId())
                .addValue("name", reservation.getName())
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("createdAt", reservation.getCreatedAt());
        String updateSql = "UPDATE pending "
                + "SET name = :name, slot_id = :slotId, created_at = :createdAt, is_deleted = 0 "
                + "WHERE id = :id";
        int affected = jdbcTemplate.update(updateSql, params);
        if (affected == 0) {
            String insertSql = "INSERT INTO pending(id, name, slot_id, created_at) "
                    + "VALUES(:id, :name, :slotId, :createdAt)";
            jdbcTemplate.update(insertSql, params);
        }
        return reservation;
    }

    @Override
    public Optional<PendingReservation> findNextPendingReservation(Long slotId) {
        String sql = "SELECT p.id AS p_id, p.name AS p_name, p.is_deleted AS p_is_deleted, p.created_at AS p_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "JOIN time_slot ts ON p.slot_id = ts.id "
                + "JOIN theme t ON ts.theme_id = t.id "
                + "JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE p.slot_id = :slotId "
                + "AND p.is_deleted = 0 "
                + "ORDER BY p.created_at ASC "
                + "LIMIT 1 "
                + "FOR UPDATE";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PendingReservation> findById(Long id) {
        String sql = "SELECT p.id AS p_id, p.name AS p_name, p.is_deleted AS p_is_deleted, p.created_at AS p_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "JOIN time_slot ts ON p.slot_id = ts.id "
                + "JOIN theme t ON ts.theme_id = t.id "
                + "JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE p.id = :id AND p.is_deleted = 0";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findAny();
    }

    @Override
    public List<PendingReservation> findExpiredReservations(LocalDate today) {
        String sql = "SELECT "
                + "p.id AS p_id, p.name AS p_name, p.is_deleted AS p_is_deleted, p.created_at AS p_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "INNER JOIN time_slot ts ON p.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE ts.date < :today AND p.is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", today);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<PendingReservation> findAllByIdIn(List<Long> reservationIds) {
        String sql = "SELECT "
                + "p.id AS p_id, p.name AS p_name, p.is_deleted AS p_is_deleted, p.created_at AS p_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM pending p "
                + "INNER JOIN time_slot ts ON p.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE p.id IN (:ids) AND p.is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource("ids", reservationIds);
        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
