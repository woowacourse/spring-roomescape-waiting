package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.exception.ErrorCode;
import roomescape.exception.KeyGenerationException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

@RequiredArgsConstructor
@Repository
public class WaitingListRepository {

    private final JdbcTemplate jdbcTemplate;

    public WaitingList save(final WaitingList waitingListWithoutId) {
        final long waitingListId = insertWaitingList(waitingListWithoutId);

        return waitingListWithoutId.withId(waitingListId);
    }

    private long insertWaitingList(final WaitingList waitingList) {
        final String sql = """
                INSERT INTO waiting_list (name, date, theme_id, time_id)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, waitingList.getName());
            preparedStatement.setDate(2, Date.valueOf(waitingList.getDate()));
            preparedStatement.setLong(3, waitingList.getTheme().getId());
            preparedStatement.setLong(4, waitingList.getReservationTime().getId());

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
