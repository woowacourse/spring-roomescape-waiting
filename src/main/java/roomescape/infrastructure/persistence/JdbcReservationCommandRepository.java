package roomescape.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationCommandRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class JdbcReservationCommandRepository implements ReservationCommandRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("member_id", "date", "time_id", "theme_id")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation create(Reservation reservation) {
        Member member = reservation.getMember();
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();
        MapSqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("member_id", member.getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());
        long id = jdbcInsert.executeAndReturnKey(parameter).longValue();
        return reservation.withId(id);
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
