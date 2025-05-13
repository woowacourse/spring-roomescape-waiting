package roomescape.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

@Component
public class ReservationDbFixture {

    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDbFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation 예약_한스_25_4_22_10시_공포(Member member, ReservationTime reservationTime, Theme theme) {
        LocalDate date = ReservationDateFixture.예약날짜_25_4_22.getDate();

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("time_id", reservationTime.getId())
                .addValue("theme_id", theme.getId())
                .addValue("member_id", member.getId())
        ).longValue();

        return Reservation.create(id, member, date, reservationTime, theme);
    }

    public Reservation 예약_생성_한스(Member member, ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        LocalDate date = reservationDate.getDate();

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("time_id", reservationTime.getId())
                .addValue("theme_id", theme.getId())
                .addValue("member_id", member.getId())
        ).longValue();

        return Reservation.create(id, member, date, reservationTime, theme);
    }
}
