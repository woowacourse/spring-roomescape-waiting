package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.support.ReservationTestHelper;

public class TransactionRollbackTest extends IntegrationTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private ReservationTestHelper helper;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private WaitingService waitingService;

    @SpyBean
    private ReservationRepository reservationRepository;

    @SpyBean
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("예약 취소 중 대기 승격에 실패하면 예약 삭제가 롤백된다")
    void 예약_취소_중_대기_승격_실패_시_예약_삭제_롤백() {
        Long timeId = helper.insertTime(LocalTime.of(10, 0));
        Long themeId = helper.insertTheme("테마A", "설명", "url");
        Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE, timeId, themeId);
        Long waitingId = helper.insertWaiting("봉구스", FUTURE_DATE, timeId, themeId, 1);

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(waitingRepository)
                .deleteById(waitingId);

        assertThatThrownBy(() -> reservationService.deleteByOwner(reservationId, "브라운"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 삭제 실패");

        assertThat(helper.findReservationCount(FUTURE_DATE, timeId, themeId)).isEqualTo(1);
        assertThat(helper.findReservationOwner(FUTURE_DATE, timeId, themeId)).isEqualTo("브라운");
        assertThat(helper.existsWaiting(waitingId)).isTrue();
    }

    @Test
    @DisplayName("관리자 예약 삭제 중 대기 승격에 실패하면 예약 삭제가 롤백된다")
    void 관리자_예약_삭제_중_대기_승격_실패_시_예약_삭제_롤백() {
        Long timeId = helper.insertTime(LocalTime.of(10, 0));
        Long themeId = helper.insertTheme("테마A", "설명", "url");
        Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE, timeId, themeId);
        Long waitingId = helper.insertWaiting("봉구스", FUTURE_DATE, timeId, themeId, 1);

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(waitingRepository)
                .deleteById(waitingId);

        assertThatThrownBy(() -> reservationService.delete(reservationId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 삭제 실패");

        assertThat(helper.findReservationCount(FUTURE_DATE, timeId, themeId)).isEqualTo(1);
        assertThat(helper.findReservationOwner(FUTURE_DATE, timeId, themeId)).isEqualTo("브라운");
        assertThat(helper.existsWaiting(waitingId)).isTrue();
    }

    @Test
    @DisplayName("대기 취소 중 순번 재정렬에 실패하면 대기 삭제가 롤백된다")
    void 대기_취소_중_순번_재정렬_실패_시_대기_삭제_롤백() {
        Long timeId = helper.insertTime(LocalTime.of(10, 0));
        Long themeId = helper.insertTheme("테마A", "설명", "url");
        helper.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
        Long waitingId = helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);
        Long nextWaitingId = helper.insertWaiting("봉구스", FUTURE_DATE, timeId, themeId, 2);

        doThrow(new RuntimeException("순번 재정렬 실패"))
                .when(waitingRepository)
                .updateOrderIndex(nextWaitingId, 1);

        assertThatThrownBy(() -> waitingService.cancelByOwner(waitingId, "콘"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("순번 재정렬 실패");

        assertThat(helper.existsWaiting(waitingId)).isTrue();
        assertThat(helper.existsWaiting(nextWaitingId)).isTrue();
    }
}
