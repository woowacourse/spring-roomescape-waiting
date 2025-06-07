package roomescape.testFixture;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.presentation.controller.dto.UserReservationCreateRequest;

public class Fixture {

    public static final Member MEMBER1_ADMIN = Member.withId(1L, "어드민", "admin@email.com", "password", Role.ADMIN);
    public static final Member MEMBER2_USER = Member.withId(2L, "브라운", "brown@email.com", "brown", Role.USER);

    public static final Theme THEME_1 = Theme.withId(1L, "테마1", "테마 1입니다.", "썸네일1");
    public static final Theme THEME_2 = Theme.withId(2L, "테마2", "테마 2입니다.", "썸네일2");

    public static final ReservationTime RESERVATION_TIME_1 = ReservationTime.withId(1L, LocalTime.of(10, 0));
    public static final ReservationTime RESERVATION_TIME_2 = ReservationTime.withId(2L, LocalTime.of(11, 0));
    public static final ReservationTime RESERVATION_TIME_3 = ReservationTime.withId(3L, LocalTime.of(12, 0));

    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    public static final GameSchedule GAME_SCHEDULE_1 = GameSchedule.withId(1L, TOMORROW, RESERVATION_TIME_1, THEME_1);
    public static final GameSchedule GAME_SCHEDULE_2 = GameSchedule.withId(1L, TOMORROW, RESERVATION_TIME_1, THEME_2);
    public static final GameSchedule GAME_SCHEDULE_3 = GameSchedule.withId(1L, TOMORROW, RESERVATION_TIME_2, THEME_2);
    public static final GameSchedule GAME_SCHEDULE_4 = GameSchedule.withId(1L, TOMORROW, RESERVATION_TIME_2, THEME_1);

    public static final Reservation RESERVATION_1 = Reservation.withId(1L, MEMBER1_ADMIN, GAME_SCHEDULE_1,
            ReservationStatus.RESERVED);
    public static final Reservation RESERVATION_2 = Reservation.withId(2L, MEMBER2_USER, GAME_SCHEDULE_2,
            ReservationStatus.RESERVED);

    public static final UserReservationCreateRequest RESERVATION_BODY = createUserReservationBody();

    public static UserReservationCreateRequest createUserReservationBody() {
        return new UserReservationCreateRequest(1L, TOMORROW, 1L);
    }

    public static ReservationCreateServiceRequest createReservationBody(Long memberId) {
        return new ReservationCreateServiceRequest(TOMORROW, 1L, 1L, memberId);
    }

    public static void resetH2TableIds(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute((Connection connection) -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
                statement.execute("TRUNCATE TABLE reservation");
                statement.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE waiting");
                statement.execute("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE game_schedule");
                statement.execute("ALTER TABLE game_schedule ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE reservation_time");
                statement.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE theme");
                statement.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
                statement.execute("TRUNCATE TABLE member");
                statement.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
                statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
            return null;
        });
    }
}
