package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.TimeSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcTimeSlotRepository implements TimeSlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TimeSlot> rowMapper = (resultSet, rowNum) -> {
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

        return TimeSlot.builder()
                .id(resultSet.getLong("s_id"))
                .date(resultSet.getDate("s_date").toLocalDate())
                .time(time)
                .theme(theme)
                .build();
    };

    @Override
    public TimeSlot save(TimeSlot slot) {
        String sql = "INSERT INTO time_slot(date, time_id, theme_id) "
                + "VALUES(:date, :timeId, :themeId)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.getDate())
                .addValue("timeId", slot.getTime().getId())
                .addValue("themeId", slot.getTheme().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return slot.withId(generatedId);
    }

    @Override
    public Optional<TimeSlot> findByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT s.id AS s_id, s.date AS s_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM time_slot s "
                + "INNER JOIN theme t ON s.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON s.time_id = rt.id "
                + "WHERE s.date = :date AND s.time_id = :timeId AND s.theme_id = :themeId";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);
        return jdbcTemplate.query(sql, params, rowMapper).stream().findAny();
    }
}
