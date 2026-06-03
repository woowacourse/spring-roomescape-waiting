package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListRow;
import roomescape.exception.ErrorCode;
import roomescape.exception.KeyGenerationException;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class WaitingListRepository {

    private final JdbcTemplate jdbcTemplate;

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
                WHERE w.id = ?
                """;
        try {
            final WaitingList waitingList = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    id
            );
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<WaitingListRow> findByName(String name) {
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
                WHERE w.name = ?
                """;

        return jdbcTemplate.query(sql, this::mapToRow, name).stream().toList();
    }

    public int findWaitingOrderByIdAndThemeAndDateAndTime(final WaitingList waitingList) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE theme_id = ? AND date = ? AND time_id = ? AND created_at <= ?;
                """;

        Integer waitingOrder = jdbcTemplate.queryForObject(sql, Integer.class,
                waitingList.getTheme().getId(),
                waitingList.getReservationDate().getDate(),
                waitingList.getReservationTime().getId(),
                Timestamp.valueOf(waitingList.getCreatedAt()
                ));

        if (waitingOrder != null) {
            return waitingOrder;
        }
        return 0;
    }

    public Optional<WaitingList> findFirstByThemeAndDateAndTimeOrderByCreatedAtAsc(Theme theme, LocalDate date, ReservationTime time) {
        final String sql = """
                SELECT
                    w.id AS waiting_list_id,
                    w.name,
                    w.date,
                    w.theme_id,
                    w.created_at,
                    t.id AS time_id,
                    t.start_at,
                    t.end_at,
                    h.name AS theme_name,
                    h.description,
                    h.thumbnail_url
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id
                WHERE w.date = ?
                  AND w.time_id = ?
                  AND w.theme_id = ?
                ORDER BY w.created_at ASC
                LIMIT 1;
                """;
        try {
            final WaitingList waitingList = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    time.getId(),
                    theme.getId(),
                    date
            );
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByNameAndThemeAndDateAndTime(final String name, final Long themeId, final LocalDate date, final Long timeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE name = ? AND theme_id = ? AND date = ? AND time_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, themeId, date, timeId);

        return count != null && count > 0;
    }

    public WaitingList save(final WaitingList waitingListWithoutId) {
        final long waitingListId = insertWaitingList(waitingListWithoutId);

        return waitingListWithoutId.withId(waitingListId);
    }

    private long insertWaitingList(final WaitingList waitingList) {
        final String sql = """
                INSERT INTO waiting_list (name, date, theme_id, time_id, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, waitingList.getName());
            preparedStatement.setDate(2, Date.valueOf(waitingList.getReservationDate().getDate()));
            preparedStatement.setLong(3, waitingList.getTheme().getId());
            preparedStatement.setLong(4, waitingList.getReservationTime().getId());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(waitingList.getCreatedAt()));

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private static long generatedIdFrom(final KeyHolder keyHolder) {
        final Number generatedKey = keyHolder.getKey();

        if (generatedKey == null) {
            throw new KeyGenerationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return generatedKey.longValue();
    }

    public void deleteById(Long id) {
        final String sql = """
                DELETE FROM waiting_list
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, id);
    }

    /**
     * ResultSet - Domain 매핑 메서드
     */
    private WaitingList mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        final ReservationTime reservationTime = ReservationTime.createWithId(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime(),
                resultSet.getTime("time_end_at").toLocalTime()
        );

        final Theme theme = Theme.createWithId(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        return WaitingList.createWithId(
                resultSet.getLong("waiting_list_id"),
                resultSet.getString("waiting_list_name"),
                resultSet.getDate("waiting_list_date").toLocalDate(),
                theme,
                reservationTime,
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }

    private WaitingListRow mapToRow(final ResultSet resultSet, final int rowNum) throws SQLException {
        WaitingList waitingList = mapToDomain(resultSet, rowNum);
        int waitingOrder = resultSet.getInt("waiting_order");

        return new WaitingListRow(waitingList, waitingOrder);
    }
}
