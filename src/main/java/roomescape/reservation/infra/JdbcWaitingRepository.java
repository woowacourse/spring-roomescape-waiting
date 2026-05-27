package roomescape.reservation.infra;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingDetail;
import roomescape.reservation.domain.repository.WaitingRepository;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public WaitingDetail save(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("theme_id", waiting.getThemeId())
                .addValue("time_id", waiting.getTimeId());

        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            Waiting saved = waiting.withId(id);

            return new WaitingDetail(saved.getId(), saved.getName(), saved.getDate(), saved.getThemeId(), saved.getTimeId(), null);
        } catch (DuplicateKeyException e) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
