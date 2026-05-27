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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
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

        return Reservation.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .date(resultSet.getDate("r_date").toLocalDate())
                .status(Status.valueOf(resultSet.getString("r_status")))
                .time(time)
                .theme(theme)
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

        return ReservationQueryResult.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .date(resultSet.getDate("r_date").toLocalDate())
                .status(Status.valueOf(resultSet.getString("r_status")))
                .pendingIndex(resultSet.getLong("pending_index"))
                .time(time)
                .theme(theme)
                .build();
    };

    @Override
    public Reservation save(final Reservation reservation) {
        String sql = "INSERT INTO reservation(name, date, time_id, theme_id, status, created_at, uniqueness_token) "
                + "VALUES(:name, :date, :timeId, :themeId, :status, :createdAt, :uniquenessToken)";
        long uniquenessToken = reservation.getStatus().equals(Status.ACTIVE) ? 0L : System.nanoTime();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("createdAt", reservation.getCreatedAt())
                .addValue("uniquenessToken", uniquenessToken);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return reservation.withId(generatedId);
    }

    @Override
    public void updateById(final Long id, final Reservation reservation) {
        String sql = "UPDATE reservation "
                + "SET date = :date, time_id = :timeId, theme_id = :themeId, status = :status, uniqueness_token = :uniquenessToken "
                + "WHERE id = :id AND is_deleted = 0";

        long uniquenessToken = reservation.getStatus().equals(Status.ACTIVE) ? 0L : System.nanoTime();

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("uniquenessToken", uniquenessToken)
                .addValue("id", id);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.id = :id AND (r.status='ACTIVE' OR r.status='PENDING') AND r.is_deleted = 0 ";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE (r.status = 'ACTIVE' OR r.status = 'PENDING') AND r.is_deleted = 0 "
                + "ORDER BY r.date ASC, rt.start_at ASC";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Reservation> findByThemeAndDate(final Long themeId, final LocalDate date) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.theme_id = :themeId AND r.date = :date AND (r.status = 'ACTIVE' OR r.status = 'PENDING') AND r.is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", date);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<ReservationQueryResult> findAllByName(final String username) {
        String sql = "SELECT "
                + "    r.id AS r_id, "
                + "    r.name AS r_name, "
                + "    r.date AS r_date, "
                + "    r.status AS r_status, "
                + "    r.created_at AS r_created_at, "
                + "    t.id AS t_id, "
                + "    t.name AS t_name, "
                + "    t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "    t.description AS t_description, "
                + "    t.duration_time AS t_duration_time, "
                + "    rt.id AS rt_id, "
                + "    rt.start_at AS rt_start_at, "
                + "("
                + "        SELECT COUNT(*) + 1 "
                + "        FROM reservation p "
                + "        WHERE p.date = r.date "
                + "          AND p.time_id = r.time_id "
                + "          AND p.theme_id = r.theme_id "
                + "          AND p.status = 'PENDING' "
                + "          AND p.is_deleted = 0 "
                + "          AND ("
                + "                 p.created_at < r.created_at "
                + "                 OR (p.created_at = r.created_at AND p.id < r.id) "
                + "              )"
                + ") AS pending_index "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.name = :username "
                + "  AND r.is_deleted = 0 "
                + "ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, Map.of("username", username), queryResultRowMapper);
    }

    @Override
    public boolean existsByReservationTime(final Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=:timeId AND (status = 'ACTIVE' OR status = 'PENDING') AND is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId), Boolean.class));
    }

    @Override
    public boolean existsByReservationTimeAndThemeAndDate(final Long timeId, final Long themeId, final LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=:timeId AND theme_id=:themeId AND date=:date AND status='ACTIVE' AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId, "themeId", themeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsByReservationTimeAndThemeAndDateAndIdNot(final Long id, final Long timeId, final Long themeId,
                                                                  final LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id != :id AND time_id=:timeId AND theme_id=:themeId AND date=:date AND status='ACTIVE' AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("id", id, "timeId", timeId, "themeId", themeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsByTheme(final Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId), Boolean.class));
    }

    @Override
    public boolean existsActiveReservationByThemeAndTime(final Long timeId, final Long themeId, final LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND time_id=:timeId AND date=:date AND status='ACTIVE')";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId, "timeId", timeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsPendingReservationByName(final Long timeId, final Long themeId, final LocalDate date,
                                                  final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND time_id=:timeId AND date=:date AND status='PENDING' AND name=:name)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("themeId", themeId, "timeId", timeId, "date", date, "name", name),
                        Boolean.class));
    }

    @Override
    public int deleteById(final Long id) {
        String sql = "DELETE FROM reservation WHERE id=:id";
        return jdbcTemplate.update(sql, Map.of("id", id));
    }

    @Override
    public void cancel(final Reservation reservation) {
        String sql = "UPDATE reservation SET status = 'CANCELED', is_deleted=:id WHERE id = :id AND (status='ACTIVE' OR status='PENDING')";
        jdbcTemplate.update(sql, Map.of("id", reservation.getId()));
    }

    @Override
    public Optional<Reservation> findNextPendingReservation(LocalDate date, Long timeId, Long themeId) {
        String sql =
                "SELECT r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, r.created_at AS r_created_at, "
                        + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                        + "t.description AS t_description, t.duration_time AS t_duration_time, "
                        + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                        + "FROM reservation r "
                        + "INNER JOIN theme t ON r.theme_id = t.id "
                        + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                        + "WHERE r.date = :date AND r.time_id = :timeId AND r.theme_id = :themeId "
                        + "AND r.status = 'PENDING' AND r.is_deleted = 0 "
                        + "ORDER BY r.created_at ASC "
                        + "LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
