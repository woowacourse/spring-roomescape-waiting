package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservationtime.service.ReservationTimeService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        ReservationResponse response = reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 1L, 1L));
        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_timeId_예외() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 999L, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND))
                .hasMessage(ErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_themeId_예외() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 1L, 999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 후 해당 시간 예약 가능")
    void 예약_삭제_성공() {
        assertThat(reservationTimeService.getAvailableTimes(LocalDate.of(2099, 12, 1), 1L)).hasSize(1);

        reservationService.deleteReservation(11L);

        assertThat(reservationTimeService.getAvailableTimes(LocalDate.of(2099, 12, 1), 1L)).hasSize(2);
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 과거_예약_취소_불가() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(PastTimeCancelException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PAST_RESERVATION_CANCEL))
                .hasMessage(ErrorCode.PAST_RESERVATION_CANCEL.getMessage());
    }

    @Test
    @DisplayName("예약 수정 성공")
    void 예약_수정_성공() {
        ReservationResponse response = reservationService.updateReservation(
                11L, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L));
        assertThat(response.date()).isEqualTo(LocalDate.of(2099, 12, 2));
    }

    @Test
    @DisplayName("이미 지난 예약은 수정할 수 없다")
    void 과거_예약_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                1L, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PAST_RESERVATION_UPDATE))
                .hasMessage(ErrorCode.PAST_RESERVATION_UPDATE.getMessage());
    }

    @Test
    @DisplayName("변경하려는 날짜·시간이 과거면 수정 불가")
    void 새시간_과거면_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                11L, new ReservationUpdateRequest(LocalDate.now().minusDays(1), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PAST_TIME_RESERVATION))
                .hasMessage(ErrorCode.PAST_TIME_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("변경하려는 시간이 이미 예약된 경우 수정 불가")
    void 중복_예약_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                12L, new ReservationUpdateRequest(LocalDate.of(2099, 12, 1), 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESERVATION))
                .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
    }
}