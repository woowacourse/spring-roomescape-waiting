package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationWaitDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/cleanup.sql")
class ReservationServiceTransactionTest {
    private static final int MEMBER_COUNT = 8;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    ReservationWaitDao reservationWaitDao;

    @MockitoSpyBean
    ReservationDao reservationDao;

    @Autowired
    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO store (name) VALUES ('강남점')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, img_url) VALUES (?, ?, ?)",
                "테스트테마", "설명", "https://img.example.com/t.jpg");
        for (int i = 1; i <= MEMBER_COUNT; i++) {
            jdbcTemplate.update(
                    "INSERT INTO member (email, password, name, role, store_id) VALUES (?, ?, ?, 'USER', NULL)",
                    "user" + i + "@email.com", "password", "사용자" + i);
        }
    }

    @Test
    void 승급도중_대기삭제_실패시_롤백() {
        long ownerId = 1L;
        long waiterId = 2L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, waiterId);

        doThrow(RuntimeException.class)
                .when(reservationWaitDao).deleteByReservationIdAndMemberId(reservationId, waiterId);
        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId, ownerId))
                .isInstanceOf(RuntimeException.class);

        Long owner = jdbcTemplate.queryForObject("SELECT member_id FROM reservation WHERE id = ?", Long.class, reservationId);
        Long waits = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?", Long.class, reservationId);

        assertThat(owner).as("양도 과정이 롤백되어 원소유자 그대로").isEqualTo(ownerId);
        assertThat(waits).as("대기 중인 예약이 롤백되지 않고 남아있음").isEqualTo(1L);
    }

    @Test
    void 승급_자체가_실패하면_대기자가_유실되지_않고_롤백된다() {
        long ownerId = 1L;
        long waiterId = 2L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (?, ?, '2026-12-01 10:00:00')",
                reservationId, waiterId);

        doThrow(RuntimeException.class)
                .when(reservationDao).updateMemberId(reservationId, waiterId);
        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId, ownerId))
                .isInstanceOf(RuntimeException.class);

        Long owner = jdbcTemplate.queryForObject("SELECT member_id FROM reservation WHERE id = ?", Long.class, reservationId);
        Long waits = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?", Long.class, reservationId);

        assertThat(owner).as("승급이 실패하면 소유자는 원소유자 그대로").isEqualTo(ownerId);
        assertThat(waits).as("승급 실패 시 대기자가 큐에서 사라지지 않아야 한다").isEqualTo(1L);
    }

    @Test
    void 대기생성후_대기조회_실패시_대기신청_롤백() {
        long ownerId = 1L;
        long waiterId = 2L;

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id, store_id) "
                        + "VALUES (?, '2027-01-01', 1, 1, 1)",
                ownerId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);

        doThrow(RuntimeException.class)
                .when(reservationWaitDao).findWaitOrder(anyLong());
        assertThatThrownBy(() -> reservationService.createWait(reservationId, waiterId))
                .isInstanceOf(RuntimeException.class);

        Long waits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?", Long.class, reservationId);

        assertThat(waits).as("대기생성 실패시 대기신청이 생성되지 않아야 한다.").isEqualTo(0L);
    }
}
