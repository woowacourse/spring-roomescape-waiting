package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

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
    public Optional<WaitingDetail> findDetailById(Long id) {
        return jdbcTemplate.query(
                """
                        SELECT w.id, w.name, w.date, w.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, w.time_id, rt.start_at
                        FROM waiting w
                        JOIN theme t ON w.theme_id = t.id
                        JOIN reservation_time rt ON w.time_id = rt.id
                        WHERE w.id = ?
                        """,
                (rs, rowNum) ->
                        new WaitingDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime()),
                id
        ).stream().findFirst();
    }

    @Override
    public Optional<Waiting> findOldestByDateAndThemeIdAndTimeId(
            LocalDate date,
            Long themeId,
            Long timeId
    ) {
        return jdbcTemplate.query(
                """
                        SELECT *
                        FROM waiting
                        WHERE date = ?
                          AND theme_id = ?
                          AND time_id = ?
                        ORDER BY id ASC
                        LIMIT 1
                        """,
                (rs, rowNum) ->
                        Waiting.of(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getLong("time_id")
                        ),
                date,
                themeId,
                timeId
        ).stream().findFirst();
    }

    @Override
    public Waiting save(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("theme_id", waiting.getThemeId())
                .addValue("time_id", waiting.getTimeId());

        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return waiting.withId(id);
        } catch (DuplicateKeyException e) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    @Override
    public List<WaitingOrderDetail> findByName(String name) {
        return jdbcTemplate.query(
                """
                        SELECT ranked.id, ranked.name, ranked.date,
                               ranked.theme_id, t.name AS theme_name, t.description, t.thumbnail_img_url,
                               ranked.time_id, rt.start_at,
                               ranked.rank
                        FROM (
                            SELECT id, name, date, theme_id, time_id,
                                ROW_NUMBER() OVER (
                                    PARTITION BY date, theme_id, time_id
                                    ORDER BY id
                                ) AS rank
                            FROM waiting
                        ) ranked
                        JOIN theme t ON ranked.theme_id = t.id
                        JOIN reservation_time rt ON ranked.time_id = rt.id
                        WHERE ranked.name = ?
                        ORDER BY ranked.date ASC
                        """,
                (rs, rowNum) -> new WaitingOrderDetail(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getLong("rank")),
                name
        );
    }

}
