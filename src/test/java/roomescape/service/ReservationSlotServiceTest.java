package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.dto.ReservationRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationSlotServiceTest {

    @Autowired
    private ReservationService reservationService;


    @DisplayName("예약 정상 테스트")
    @Test
    void 예약_정상_테스트() {
        LocalDateTime mockToday = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("김철수", mockToday.toLocalDate().plusDays(1), 1L, 1L);
        assertThatCode(() -> reservationService.save(mockToday, request)).doesNotThrowAnyException();
    }

    @DisplayName("지나간 날짜·시간에 대한 예약 생성은 불가능하다.")
    @Test
    void 지나간_날짜_예약_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("김철수", now.toLocalDate().minusDays(1), 1L, 1L);
        assertThatThrownBy(() -> reservationService.save(now, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAST_DATE_RESERVATION.getMessage());
    }

    @DisplayName("지나간 시간에 대한 예약 생성은 불가능하다.")
    @Test
    void 지나간_시간_예약_예외_테스트() {
        LocalDateTime mockToday= LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));

        ReservationRequest request = new ReservationRequest("김철수", mockToday.toLocalDate(), 1L, 1L);
        assertThatThrownBy(() -> reservationService.save(mockToday, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAST_DATE_RESERVATION.getMessage());
    }

    @DisplayName("지나간 날짜/시간에 대한 예약 취소는 불가능하다.")
    @Test
    void 지나간_시간_예약_취소_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> reservationService.delete(now,1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION.getMessage());
    }

//    @DisplayName("같은 날짜+시간+테마에 이미 예약이 있으면 중복 예약을 거부한다.")
//    @Test
//    void 중복_예약_예외_테스트() {
//        LocalDateTime today= LocalDateTime.now();
//
//        ReservationRequest request = new ReservationRequest("김철수", today.toLocalDate().plusDays(2), 2L, 1L);
//        reservationService.save(today, request);
//
//        assertThatThrownBy(() -> reservationService.save(today, request))
//                .isInstanceOf(CustomException.class)
//                .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
//    }

//    @DisplayName("수정하려는 날짜/시간에 이미 예약이 있으면 예외를 던진다.")
//    @Test
//    void 예약_수정_중복_예외_테스트() {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDate today = now.toLocalDate();
//        ReservationRequest request = new ReservationRequest("이영희", today.plusDays(2), 2L, 1L);
//        ReservationUpdateRequest reservationRequest = new ReservationUpdateRequest("이영희", LocalDate.of(2026, 6, 30), 5L);
//
//        ReservationResponse response = reservationService.save(now, request);
//        waitingService.save(now, new WaitingRequest("보조대기", response.id()));
//
//        assertThatThrownBy(() -> reservationService.update(response.waitingResponse().id(), now, reservationRequest))
//                .isInstanceOf(CustomException.class)
//                .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
//    }
//
//        @DisplayName("수정하려는 날짜/시간에 예약이 없으면 예약 수정된다.")
//        @Test
//        void 예약_수정_정상_테스트() {
//            LocalDateTime now = LocalDateTime.now();
//            LocalDate today = now.toLocalDate();
//            ReservationRequest request = new ReservationRequest("김철수", today.plusDays(30), 2L, 1L);
//            ReservationUpdateRequest reservationRequest = new ReservationUpdateRequest("김철수",today.plusDays(30), 3L);
//
//            ReservationResponse response = reservationService.save(now, request);
//            waitingService.save(now, new WaitingRequest("보조대기", response.id()));
//
//            assertThatCode(() -> reservationService.update(response.waitingResponse().id(), now, reservationRequest))
//                    .doesNotThrowAnyException();
//        }

}
