package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:promotiontest;DB_CLOSE_DELAY=-1")
@Transactional
class ReservationServicePromotionTest {

    private static final Long THEME_ID = 1L;
    private static final Long ORIGIN_TIME_ID = 3L; // data.sql: 12:00
    private static final Long NEW_TIME_ID = 5L;    // data.sql: 14:00

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationWaitingDao reservationWaitingDao;

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime originTime = new ReservationTime(ORIGIN_TIME_ID, LocalTime.of(12, 0));

    @Test
    @DisplayName("예약을 다른 슬롯으로 옮기면, 원래 슬롯의 첫 대기자가 그 슬롯의 예약으로 승격된다")
    void 수정시_원래_슬롯_대기자가_예약으로_승격된다() {
        // given: 로치 예약(원래 슬롯) + 브라운 대기(같은 슬롯)
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));
        reservationWaitingDao.insert(new ReservationWaiting("브라운", THEME_ID, date, originTime));

        // when: 로치 예약을 새 슬롯으로 이동 (원래 슬롯이 비워짐)
        reservationService.modifyDateTimeByName(reserved.getId(), "로치", THEME_ID, date, NEW_TIME_ID);

        // then: 브라운 대기가 사라지고, 브라운이 원래 슬롯의 예약으로 승격된다
        assertThat(reservationWaitingDao.selectByName("브라운")).isEmpty();

        List<Reservation> promoted = reservationDao.selectByName("브라운");
        assertThat(promoted).hasSize(1);
        assertThat(promoted.get(0).getTime().getId()).isEqualTo(ORIGIN_TIME_ID);
    }

    @Test
    @DisplayName("원래 슬롯에 대기자가 없으면 승격이 일어나지 않는다")
    void 수정시_원래_슬롯에_대기자가_없으면_승격하지_않는다() {
        // given: 로치 예약만 존재, 대기 없음
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));

        // when: 로치 예약을 새 슬롯으로 이동 (원래 슬롯이 비워짐)
        reservationService.modifyDateTimeByName(reserved.getId(), "로치", THEME_ID, date, NEW_TIME_ID);

        // then: 대기자가 없으므로 원래 슬롯으로 승격된 예약이 없다
        assertThat(reservationDao.existsByThemeIdAndDateAndTimeId(THEME_ID, date, ORIGIN_TIME_ID))
                .isFalse();
    }

    @Test
    @DisplayName("예약을 취소하면, 그 슬롯의 첫 대기자가 예약으로 승격된다")
    void 취소시_슬롯_대기자가_예약으로_승격된다() {
        // given: 로치 예약 + 브라운 대기(같은 슬롯)
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));
        reservationWaitingDao.insert(new ReservationWaiting("브라운", THEME_ID, date, originTime));

        // when: 로치 예약 취소
        reservationService.deleteById(reserved.getId());

        // then: 브라운 대기가 사라지고, 브라운이 그 슬롯의 예약으로 승격된다
        assertThat(reservationWaitingDao.selectByName("브라운")).isEmpty();

        List<Reservation> promoted = reservationDao.selectByName("브라운");
        assertThat(promoted).hasSize(1);
        assertThat(promoted.get(0).getTime().getId()).isEqualTo(ORIGIN_TIME_ID);
    }

    @Test
    @DisplayName("예약을 취소할 때 대기자가 없으면 승격 없이 슬롯이 비워진다")
    void 취소시_대기자가_없으면_승격하지_않는다() {
        // given: 로치 예약만 존재, 대기 없음
        Reservation reserved = reservationDao.insert(new Reservation("로치", THEME_ID, date, originTime));

        // when: 로치 예약 취소
        reservationService.deleteById(reserved.getId());

        // then: 그 슬롯에 예약이 없다 (단순 삭제)
        assertThat(reservationDao.existsByThemeIdAndDateAndTimeId(THEME_ID, date, ORIGIN_TIME_ID))
                .isFalse();
    }
}
