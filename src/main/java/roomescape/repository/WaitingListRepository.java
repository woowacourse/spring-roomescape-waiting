package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.WaitingList;
import roomescape.exception.ErrorCode;
import roomescape.exception.KeyGenerationException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;

@RequiredArgsConstructor
@Repository
public class WaitingListRepository {

    private final JdbcTemplate jdbcTemplate;


    public boolean existsByNameAndThemeAndDateAndTime(String name, Long themeId, LocalDate date, Long timeId) {
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
}
