package roomescape.integration.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Component
public class ReservationDbFixture {
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationDbFixture(final JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation 예약_25_4_22(final ReservationTime time, final Theme theme, final Member member) {
        ReservationDate date = ReservationDateFixture.예약날짜_25_4_22;
        return createReservation(date, time, theme, member);
    }

    public Reservation 예약_생성(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        return createReservation(date, time, theme, member);
    }

    public Reservation createReservation(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        Long id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("member_id", member.getId())
                .addValue("date", date.getDate())
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId())
        ).longValue();
        return new Reservation(id, member, date, time, theme);

    }


}
