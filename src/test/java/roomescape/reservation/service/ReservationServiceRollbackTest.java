package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:rollbacktest;DB_CLOSE_DELAY=-1")
class ReservationServiceRollbackTest {

    private static final Long THEME_ID = 1L;
    private static final Long ORIGIN_TIME_ID = 3L; // data.sql: 12:00
    private static final Long NEW_TIME_ID = 5L;    // data.sql: 14:00

    @Autowired
    private ReservationService reservationService;
    @MockitoSpyBean
    private ReservationDao reservationDao;
    @Autowired
    private ReservationWaitingDao reservationWaitingDao;
    @Autowired
    private JdbcTemplate jdbc;

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime originTime = new ReservationTime(ORIGIN_TIME_ID, LocalTime.of(12, 0));

    @BeforeEach
    void cleanUp() {
        jdbc.execute("delete from reservation_waiting");
        jdbc.execute("delete from reservation");
    }

    @Test
    @DisplayName("예약 취소-승격 중 승격 insert 가 실패하면 예약·대기 삭제가 모두 롤백된다")
    void 취소_승격_중간실패시_전체롤백() {
        // given: 예약(로치) + 대기(브라운) 가 같은 슬롯에 존재
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));
        ReservationWaiting waiting = reservationWaitingDao.insert(new ReservationWaiting("브라운", THEME_ID, date, originTime));

        // 승격 마지막 단계(insert)에서 실패하도록 주입
        doThrow(new RuntimeException("강제 실패")).when(reservationDao).insert(any(Reservation.class));

        // when: 예약 취소 → 승격 도중 insert 실패
        assertThatThrownBy(() -> reservationService.deleteById(reserved.getId()))
                .isInstanceOf(RuntimeException.class);

        // then: 트랜잭션 롤백으로 예약·대기 모두 그대로 (부분 삭제 없음)
        assertThat(reservationDao.selectById(reserved.getId()))
                .as("취소 대상 예약이 롤백되어 그대로 존재해야 한다")
                .isPresent();
        assertThat(reservationWaitingDao.selectById(waiting.getId()))
                .as("승격 대상 대기가 롤백되어 그대로 존재해야 한다")
                .isPresent();
    }

    @Test
    @DisplayName("예약 수정-승격 중 승격 insert 가 실패하면 예약 이동·대기 삭제가 모두 롤백된다")
    void 수정_승격_중간실패시_전체롤백() {
        // given: 예약(로치)@원래슬롯 + 대기(브라운)@원래슬롯
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));
        ReservationWaiting waiting = reservationWaitingDao.insert(new ReservationWaiting("브라운", THEME_ID, date, originTime));

        doThrow(new RuntimeException("강제 실패")).when(reservationDao).insert(any(Reservation.class));

        // when: 예약을 새 슬롯으로 수정 → 원래 슬롯 승격 도중 insert 실패
        assertThatThrownBy(() ->
                reservationService.modifyDateTimeByName(reserved.getId(), "로치", THEME_ID, date, NEW_TIME_ID))
                .isInstanceOf(RuntimeException.class);

        // then: 예약은 원래 슬롯 그대로(이동 롤백) & 대기 그대로
        Reservation after = reservationDao.selectById(reserved.getId()).orElseThrow();
        assertThat(after.getTime().getId())
                .as("수정이 롤백되어 예약이 원래 슬롯에 남아야 한다")
                .isEqualTo(ORIGIN_TIME_ID);
        assertThat(reservationWaitingDao.selectById(waiting.getId()))
                .as("승격 대상 대기가 롤백되어 그대로 존재해야 한다")
                .isPresent();
    }
}
