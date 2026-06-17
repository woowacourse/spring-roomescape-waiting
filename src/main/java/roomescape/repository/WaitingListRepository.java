package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class WaitingListRepository {

    private final JdbcTemplate jdbcTemplate;

    public WaitingList save(final WaitingList waitingListWithoutId) {
        try {
            final long waitingListId = insertWaitingList(waitingListWithoutId);
            return waitingListWithoutId.withId(waitingListId);
        } catch (final DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }

    public boolean deleteById(Long id) {
        final String sql = """
                DELETE FROM waiting_list
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, id) > 0;
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
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
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

    public List<WaitingList> findByName(String name) {
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
                    h.price AS theme_price
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id 
                WHERE w.name = ?
                """;

        return jdbcTemplate.query(sql, this::mapToDomain, name).stream().toList();
    }

    public int findWaitingOrderByDateAndTimeAndTheme(final WaitingList waitingList) {
        final String sql = """
                SELECT ranking
                FROM (
                    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at ASC, id ASC) as ranking
                    FROM waiting_list
                    WHERE date = ? AND time_id = ? AND theme_id = ?
                ) as ranked_list
                WHERE id = ?
                """;

        Integer waitingOrder = jdbcTemplate.queryForObject(sql, Integer.class,
                waitingList.getReservationDate().date(),
                waitingList.getReservationTime().getId(),
                waitingList.getTheme().getId(),
                waitingList.getId()
        );

        if (waitingOrder != null) {
            return waitingOrder;
        }
        return 0;
    }

    public Optional<WaitingList> findFirstBySlot(final LocalDate date, final Long timeId, final Long themeId) {
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
                    h.price AS theme_price
                FROM waiting_list w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme h ON w.theme_id = h.id 
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.created_at ASC, w.id ASC
                LIMIT 1
                """;
        try {
            final WaitingList waitingList = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    date,
                    timeId,
                    themeId
            );
            return Optional.of(waitingList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByNameAndDateAndTimeIdAndThemeId(final String name, final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, date, timeId, themeId);

        return count != null && count > 0;
    }

    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);

        return count != null && count > 0;
    }

    public boolean existsByTimeId(final Long timeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE time_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);

        return count != null && count > 0;
    }

    public boolean existsByThemeId(final Long themeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM waiting_list
                WHERE theme_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);

        return count != null && count > 0;
    }

    private long insertWaitingList(final WaitingList waitingList) {
        final String sql = """
                INSERT INTO waiting_list (name, date, time_id, theme_id, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, waitingList.getName());
            preparedStatement.setDate(2, Date.valueOf(waitingList.getReservationDate().date()));
            preparedStatement.setLong(3, waitingList.getReservationTime().getId());
            preparedStatement.setLong(4, waitingList.getTheme().getId());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(waitingList.getCreatedAt()));

            return preparedStatement;
        }, keyHolder);

        return JdbcUtil.extractGeneratedKey(keyHolder);
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
                resultSet.getString("theme_thumbnail_url"),
                resultSet.getLong("theme_price")
        );

        return WaitingList.createWithId(
                resultSet.getLong("waiting_list_id"),
                resultSet.getString("waiting_list_name"),
                resultSet.getDate("waiting_list_date").toLocalDate(),
                reservationTime,
                theme,
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
