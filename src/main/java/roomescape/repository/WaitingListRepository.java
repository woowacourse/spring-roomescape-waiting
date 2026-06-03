package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListRow;

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
                INSERT INTO waiting_list (name, date, theme_id, time_id, created_at)
                VALUES (:name, :date, :themeId, :timeId, :createdAt)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", waitingListWithoutId.getName())
                .addValue("date", Date.valueOf(waitingListWithoutId.getReservationDate().getDate()))
                .addValue("themeId", waitingListWithoutId.getTheme().getId())
                .addValue("timeId", waitingListWithoutId.getReservationTime().getId())
                .addValue("createdAt", Timestamp.valueOf(waitingListWithoutId.getCreatedAt()));

        jdbcTemplate.update(sql, param, keyHolder);

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
            MapSqlParameterSource param = new MapSqlParameterSource()
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

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name);

        return jdbcTemplate.query(sql, param, waitingListRowRowMapper()).stream().toList();
    }

    public int findWaitingOrderByIdAndThemeAndDateAndTime(final WaitingList waitingList) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE theme_id = :themeId AND date = :date AND time_id = :timeId AND created_at <= :createdAt;
                """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", waitingList.getTheme().getId())
                .addValue("date", waitingList.getReservationDate().getDate())
                .addValue("timeId", waitingList.getReservationTime().getId())
                .addValue("createdAt", Timestamp.valueOf(waitingList.getCreatedAt()));

        Integer waitingOrder = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return waitingOrder != null ? waitingOrder : 0;
    }

    public Optional<WaitingList> findFirstByThemeAndDateAndTimeOrderByCreatedAtAsc(final Theme theme, final LocalDate date, final ReservationTime time) {
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
            MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("date", date)
                    .addValue("timeId", time.getId())
                    .addValue("themeId", theme.getId());
            final WaitingList waitingList = jdbcTemplate.queryForObject(sql, param, waitingListRowMapper());
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByNameAndThemeAndDateAndTime(final String name, final Long themeId, final LocalDate date, final Long timeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE name = :name AND theme_id = :themeId AND date = :date AND time_id = :timeId
                """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("themeId", themeId)
                .addValue("date", date)
                .addValue("timeId", timeId);

        Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    public void deleteById(final Long id) {
        final String sql = """
                DELETE FROM waiting_list
                WHERE id = :id
                """;

        MapSqlParameterSource param = new MapSqlParameterSource()
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
