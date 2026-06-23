package roomescape.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationwait.dto.WaitingResult;
import roomescape.reservationwait.exception.ReservationWaitAlreadyExistsException;
import roomescape.reservationwait.exception.PendingReservationWaitNotAllowedException;

@JdbcTest
@ActiveProfiles("test")
@Import({ReservationWaitService.class, ReservationWaitDao.class, ReservationDao.class})
public class ReservationWaitServiceIntegrationTest {

    private static final long BROWN_ID = 1L;
    private static final long YOUNGHEE_ID = 3L;
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

    private static final String INSERT_THREE_MEMBERS_SQL = """
              INSERT INTO member (id, email, password, name, role)
              VALUES (1, 'brown@email.com', 'password', '브라운', 'USER'),
                     (2, 'jeongkong@email.com', 'password', '정콩이', 'USER'),
                     (3, 'younghee@email.com', 'password', '영희', 'USER');
              """;

    private static final String INSERT_TIE_CREATED_AT_WAITS_SQL = """
              INSERT INTO reservation_wait (id, reservation_id, member_id, created_at)
              VALUES (1, 1, 1, '2026-11-01 10:00:00'),
                     (2, 1, 3, '2026-11-01 10:00:00');
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
            INSERT_JEONGKONG_RESERVATION_SQL
    })
    void 같은_사용자는_같은_슬롯에_예약대기를_걸_수_없다() {
        // given
        reservationWaitService.createReservationWait(BROWN_ID, RESERVATION_ID);

        // when & then
        assertThatThrownBy(() -> reservationWaitService.createReservationWait(BROWN_ID, RESERVATION_ID))
                .isInstanceOf(ReservationWaitAlreadyExistsException.class);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ? AND member_id = ?",
                Integer.class, RESERVATION_ID, BROWN_ID);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_THREE_MEMBERS_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_JEONGKONG_RESERVATION_SQL,
            INSERT_TIE_CREATED_AT_WAITS_SQL
    })
    void 동일_시각_대기자라도_먼저_신청한_사용자가_앞순번을_가진다() {
        // given: 정콩이가 예약자, BROWN(id=1)·영희(id=3)가 동일 createdAt으로 대기

        // when: 각자 본인의 대기 목록 조회
        List<WaitingResult> brownWaitings = reservationWaitService.getWaitings(BROWN_ID);
        List<WaitingResult> youngheeWaitings = reservationWaitService.getWaitings(YOUNGHEE_ID);

        // then: 먼저 INSERT 한 BROWN 이 1번, 영희가 2번
        assertThat(brownWaitings).hasSize(1);
        assertThat(brownWaitings.get(0).order()).isEqualTo(1L);

        assertThat(youngheeWaitings).hasSize(1);
        assertThat(youngheeWaitings.get(0).order()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_예약에는_대기를_걸_수_없다() {
        // given: 빈 DB

        // when & then: 없는 reservation(999) 으로 대기 신청 시도
        assertThatThrownBy(() -> reservationWaitService.createReservationWait(BROWN_ID, 999L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            "INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id, status) "
                    + "VALUES (1, 2, '2026-12-01', 1, 1, 1, 'PENDING')"
    })
    void 결제가_완료되지_않은_예약에는_대기를_걸_수_없다() {
        assertThatThrownBy(() -> reservationWaitService.createReservationWait(BROWN_ID, RESERVATION_ID))
                .isInstanceOf(PendingReservationWaitNotAllowedException.class);
    }
}
