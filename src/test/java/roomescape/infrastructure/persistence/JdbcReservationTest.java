package roomescape.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.MemberCommandRepository;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;
import roomescape.domain.ThemeRepository;

@JdbcTest
@Sql("/truncate.sql")
public abstract class JdbcReservationTest {
    protected final JdbcTemplate jdbcTemplate;
    protected final SimpleJdbcInsert jdbcInsert;
    protected final ThemeRepository themeRepository;
    protected final ReservationTimeRepository reservationTimeRepository;
    protected final MemberCommandRepository memberCommandRepository;

    @Autowired
    public JdbcReservationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("member_id", "date", "time_id", "theme_id")
                .usingGeneratedKeyColumns("id");
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        this.memberCommandRepository = new JdbcMemberCommandRepository(jdbcTemplate);
    }

    protected Reservation createReservation() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.create(new Theme(new ThemeName("theme1"), "desc", "url"));
        Member member = memberCommandRepository.create(new Member(new PlayerName("test"), new Email("test@email.com"), new Password("wootecoCrew6!"), Role.BASIC));
        LocalDate date = LocalDate.of(2024, 12, 25);
        long id = jdbcInsert.executeAndReturnKey(Map.of(
                "member_id", member.getId(),
                "date", date,
                "time_id", reservationTime.getId(),
                "theme_id", theme.getId()
        )).longValue();
        return new Reservation(id, member, date, reservationTime, theme);
    }
}
