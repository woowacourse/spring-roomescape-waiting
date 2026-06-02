package roomescape.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.ReservationDao;
import roomescape.reservationwait.exception.ReservationWaitAlreadyExistsException;

@JdbcTest
@ActiveProfiles("test")
@Import({ReservationWaitService.class, ReservationWaitDao.class, ReservationDao.class})
public class ReservationWaitServiceIntegrationTest {

    private static final long BROWN_ID = 1L;
    private static final long RESERVATION_ID = 1L;

    private static final String INSERT_DEFAULT_STORE_SQL = """
              INSERT INTO store (id, name)
              VALUES (1, '강남점');
              """;

    private static final String INSERT_TWO_MEMBERS_SQL = """
              INSERT INTO member (id, email, password, name, role)
              VALUES (1, 'brown@email.com', 'password', '브라운', 'USER'),
                     (2, 'jeongkong@email.com', 'password', '정콩이', 'USER');
              """;

    private static final String INSERT_DEFAULT_THEME_SQL = """
              INSERT INTO theme (id, name, description, img_url)
              VALUES (1, '테마', '설명', 'https://example.com/img.jpg');
              """;

    private static final String INSERT_DEFAULT_TIME_SQL = """
              INSERT INTO reservation_time (id, start_at)
              VALUES (1, '10:00');
              """;

    private static final String INSERT_JEONGKONG_RESERVATION_SQL = """
              INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
              VALUES (1, 2, '2026-12-01', 1, 1, 1);
              """;

    private static final String INSERT_BROWN_WAIT_SQL = """
              INSERT INTO reservation_wait (id, reservation_id, member_id)
              VALUES (1, 1, 1);
              """;

    private final ReservationWaitService reservationWaitService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationWaitServiceIntegrationTest(ReservationWaitService reservationWaitService,
                                                 JdbcTemplate jdbcTemplate) {
        this.reservationWaitService = reservationWaitService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_JEONGKONG_RESERVATION_SQL
    })
    void 예약대기를_생성한다() {
        // given: 정콩이가 예약, BROWN이 대기 가능

        // when: BROWN 이 대기 신청
        ReservationWait result = reservationWaitService.createReservationWait(BROWN_ID, RESERVATION_ID);

        // then: 반환된 객체 검증
        assertThat(result.getId()).isPositive();
        assertThat(result.getMemberId()).isEqualTo(BROWN_ID);
        assertThat(result.getReservationId()).isEqualTo(RESERVATION_ID);
        assertThat(result.getCreatedAt()).isNotNull();

        // then: DB 에 row 가 실제로 들어갔는지 검증
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ? AND member_id = ?",
                Integer.class, RESERVATION_ID, BROWN_ID);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_JEONGKONG_RESERVATION_SQL,
            INSERT_BROWN_WAIT_SQL
    })
    void 같은_사용자는_같은_슬롯에_예약대기를_걸_수_없다() {
        // given: BROWN이 이미 대기 중

        // when & then: 또 신청 시도 → UNIQUE 위반
        assertThatThrownBy(() -> reservationWaitService.createReservationWait(BROWN_ID, RESERVATION_ID))
                .isInstanceOf(ReservationWaitAlreadyExistsException.class);

        // then: 기존 대기 row 만 유지 (중복 row 안 생김)
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ? AND member_id = ?",
                Integer.class, RESERVATION_ID, BROWN_ID);
        assertThat(count).isEqualTo(1);
    }
}
