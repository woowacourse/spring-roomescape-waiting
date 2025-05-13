package roomescape.testFixture;

import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class JdbcHelper {
    public static void insertTheme(JdbcTemplate template, Theme theme) {
        template.update("INSERT INTO theme (name, description, thumbnail) VALUES (?,?,?)",
                theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    public static void insertReservationTime(JdbcTemplate template, ReservationTime reservationTime) {
        template.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                reservationTime.getStartAt());
    }

    public static void insertMember(JdbcTemplate template, Member member) {
        if (member.getId() == null) {
            template.update("INSERT INTO member(name , email, password, role) VALUES (?,?,?,?)",
                    member.getName(), member.getEmail(), member.getPassword(), member.getRole().name());
            return;
        }

        template.update("INSERT INTO member(id, name , email, password, role) VALUES (?,?,?,?,?)",
                member.getId(), member.getName(), member.getEmail(), member.getPassword(), member.getRole().name());
    }

    public static void insertReservationTimes(JdbcTemplate template, ReservationTime... reservationTimes) {
        Arrays.stream(reservationTimes)
                .forEach(reservationTime -> insertReservationTime(template, reservationTime));
    }

    public static void insertReservation(JdbcTemplate template, Reservation reservation) {
        template.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                reservation.getMember().getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId()
        );
    }
}
