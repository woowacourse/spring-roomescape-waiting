package roomescape.helper;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Component
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;
    private Theme theme;
    private ReservationTime time;
    private Reservation reservation;
    private Member member;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void execute() {
        theme = createTheme();
        time = createInitTime();
        member = createMember();
        reservation = createInitReservation(time, theme, member);
    }

    private Member createMember() {
        jdbcTemplate.update("INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", "이름",
                "email@email.com", "password", "admin");
        return new Member(1L, "이름", "email@email.com", "password");
    }

    private Theme createTheme() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "레벨2", "내용이다.", "https://www.naver.com/");
        return new Theme(1L, "레벨2", "내용이다.", "https://www.naver.com/");
    }

    private ReservationTime createInitTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        return new ReservationTime(1L, LocalTime.of(10, 0));
    }

    private Reservation createInitReservation(ReservationTime time, Theme theme, Member member) {
        jdbcTemplate.update("INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                "2024-08-05", "1", "1", "1");
        return new Reservation(1L, LocalDate.of(2024, 8, 5), time, theme, member);
    }
}
