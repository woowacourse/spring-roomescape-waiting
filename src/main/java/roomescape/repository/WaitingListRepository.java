package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class WaitingListRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public WaitingList save(final WaitingList waitingListWithoutId) {
        final String sql = """
                INSERT INTO waiting_list (name, date, time_id, theme_id, created_at)
                VALUES (:name, :date, :timeId, :themeId, :createdAt)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", waitingListWithoutId.getName())
                .addValue("date", Date.valueOf(waitingListWithoutId.getReservationDate().getDate()))
                .addValue("timeId", waitingListWithoutId.getReservationTime().getId())
                .addValue("themeId", waitingListWithoutId.getTheme().getId())
                .addValue("createdAt", Timestamp.valueOf(waitingListWithoutId.getCreatedAt()));

        try {
            jdbcTemplate.update(sql, param, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        final long waitingListId = keyHolder.getKey().longValue();
        return waitingListWithoutId.withId(waitingListId);
    }

    public Optional<WaitingList> findById(final Long id) {
        final String sql = """
                SELECT
                    w.id AS waiting_list_id,
                    w.name AS waiting_list_name,
                    w.date AS waiting_list_date,
                    w.theme_id AS theme_id,
                    w.created_at,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id
                WHERE w.id = :id
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", id);
            final WaitingList waitingList = jdbcTemplate.queryForObject(sql, param, waitingListRowMapper());
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<WaitingListRow> findByName(final String name) {
        final String sql = """
                SELECT
                    w.id AS waiting_list_id,
                    w.name AS waiting_list_name,
                    w.date AS waiting_list_date,
                    w.theme_id AS theme_id,
                    w.created_at,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    wc.waiting_order
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id
                JOIN (
                    SELECT
                        w1.id,
                        COUNT(w2.id) AS waiting_order
                    FROM waiting_list w1
                    JOIN waiting_list w2
                        ON w1.theme_id = w2.theme_id
                               AND w1.date = w2.date
                               AND w1.time_id = w2.time_id
                               AND w2.created_at <= w1.created_at
                    GROUP BY w1.id
                ) wc ON w.id = wc.id
                WHERE w.name = :name
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name);

        return jdbcTemplate.query(sql, param, waitingListRowRowMapper()).stream().toList();
    }

    public int findWaitingOrderByDateAndTimeIdAndThemeId(final WaitingList waitingList) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE date = :date AND time_id = :timeId AND theme_id = :themeId AND created_at <= :createdAt;
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", waitingList.getReservationDate().getDate())
                .addValue("timeId", waitingList.getReservationTime().getId())
                .addValue("themeId", waitingList.getTheme().getId())
                .addValue("createdAt", Timestamp.valueOf(waitingList.getCreatedAt()));

        final Integer waitingOrder = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return waitingOrder != null ? waitingOrder : 0;
    }

    public Optional<WaitingList> findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(final LocalDate date, final ReservationTime time, final Theme theme) {
        final String sql = """
                SELECT
                    w.id AS waiting_list_id,
                    w.name AS waiting_list_name,
                    w.date AS waiting_list_date,
                    w.theme_id AS theme_id,
                    w.created_at,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id
                WHERE w.date = :date
                  AND w.time_id = :timeId
                  AND w.theme_id = :themeId
                ORDER BY w.created_at ASC
                LIMIT 1;
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("date", date)
                    .addValue("timeId", time.getId())
                    .addValue("themeId", theme.getId());
            final WaitingList waitingList = jdbcTemplate.queryForObject(sql, param, waitingListRowMapper());
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByNameAndDateAndTimeIdAndThemeId(final String name, final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE name = :name AND date = :date AND time_id = :timeId AND theme_id = :themeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        final Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public void deleteById(final Long id) {
        final String sql = """
                DELETE FROM waiting_list
                WHERE id = :id
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);

        jdbcTemplate.update(sql, param);
    }

    private RowMapper<WaitingList> waitingListRowMapper() {
        return (rs, rowNum) -> {
            final ReservationTime reservationTime = ReservationTime.createWithId(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime(),
                    rs.getTime("time_end_at").toLocalTime()
            );

            final Theme theme = Theme.createWithId(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url")
            );

            return WaitingList.createWithId(
                    rs.getLong("waiting_list_id"),
                    rs.getString("waiting_list_name"),
                    rs.getDate("waiting_list_date").toLocalDate(),
                    theme,
                    reservationTime,
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
        };
    }

    private RowMapper<WaitingListRow> waitingListRowRowMapper() {
        return (rs, rowNum) -> {
            final WaitingList waitingList = waitingListRowMapper().mapRow(rs, rowNum);
            final int waitingOrder = rs.getInt("waiting_order");
            return new WaitingListRow(waitingList, waitingOrder);
        };
    }
}
