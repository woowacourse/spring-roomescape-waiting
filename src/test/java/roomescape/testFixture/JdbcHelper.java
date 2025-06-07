package roomescape.testFixture;

import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.entity.Waiting;

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

    public static void insertGameSchedule(JdbcTemplate template, GameSchedule gameSchedule) {
        template.update("INSERT INTO game_schedule (date, time_id, theme_id) VALUES (?, ?, ?)",
                gameSchedule.getDate(),
                gameSchedule.getTime().getId(),
                gameSchedule.getTheme().getId()
        );
    }

    public static void insertReservation(JdbcTemplate template, Reservation reservation) {
        template.update("INSERT INTO reservation (member_id, game_schedule_id, status) VALUES (?, ?, ?)",
                reservation.getMember().getId(),
                reservation.getGameSchedule().getId(),
                reservation.getStatus().name()
        );
    }

    public static void insertWaiting(JdbcTemplate template, Waiting waiting) {
        template.update("INSERT INTO waiting (member_id, game_schedule_id, status) VALUES (?, ?, ?)",
                waiting.getMember().getId(),
                waiting.getGameSchedule().getId(),
                waiting.getStatus().name()
        );
    }
}
