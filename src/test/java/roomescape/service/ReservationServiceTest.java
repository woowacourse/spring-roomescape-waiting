package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        LocalDateTime mockToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));

        ReservationRequest request = new ReservationRequest("김철수", mockToday.toLocalDate(), 1L, 1L);
        assertThatThrownBy(() -> reservationService.save(mockToday, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAST_DATE_RESERVATION.getMessage());
    }

    @DisplayName("지나간 날짜/시간에 대한 예약 취소는 불가능하다.")
    @Test
    void 지나간_시간_예약_취소_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> reservationService.delete(now, 1L, "김철수"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION.getMessage());
    }

    @DisplayName("이미 날짜가 지난 예약 대기는 취소할 수 없다.")
    @Test
    void 지나간_시간_예약_대기_취소_예외_테스트() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 27, 0, 0);
        ReservationResponse waiting = reservationService.findAllByName("과거대기").get(0);

        assertThatThrownBy(() -> reservationService.delete(now, waiting.reservationId(), "과거대기"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION.getMessage());
    }

    @DisplayName("이미 예약된 슬롯에 다른 사용자가 예약하면 대기 순번을 가진 예약으로 등록된다.")
    @Test
    void 예약된_슬롯_대기_등록_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(2);
        ReservationRequest firstRequest = new ReservationRequest("김철수", reservationDate, 2L, 1L);
        ReservationRequest secondRequest = new ReservationRequest("이영희", reservationDate, 2L, 1L);

        ReservationResponse firstResponse = reservationService.save(now, firstRequest);
        ReservationResponse secondResponse = reservationService.save(now.plusSeconds(1), secondRequest);

        assertThat(firstResponse.order()).isZero();
        assertThat(secondResponse.order()).isEqualTo(1);
    }

    @DisplayName("예약이 확정되면 결제 주문 정보가 응답에 포함된다.")
    @Test
    void 예약_확정시_결제_주문_정보_응답_테스트() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("김철수", now.toLocalDate().plusDays(70), 2L, 1L);

        ReservationResponse response = reservationService.save(now, request);

        assertThat(response.status()).isEqualTo("PAYMENT_PENDING");
        assertThat(response.order()).isZero();
        assertThat(response.orderId()).matches("[A-Za-z0-9_-]{6,64}");
        assertThat(response.paymentStatus()).isEqualTo("PENDING");
        assertThat(response.paymentKey()).isNull();
        assertThat(response.amount()).isEqualTo(10000L);
    }

    @DisplayName("결제 확정 예약은 주문번호, 결제 상태, paymentKey, 금액이 함께 조회된다.")
    @Test
    void 결제_확정_예약_결제_정보_조회() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("결제확정조회", now.toLocalDate().plusDays(72), 2L, 1L);
        ReservationResponse response = reservationService.save(now, request);
        Long paymentOrderId = findPaymentOrderId(response.orderId());

        jdbcTemplate.update("UPDATE payment_order SET payment_status = 'CONFIRMED' WHERE id = ?", paymentOrderId);
        jdbcTemplate.update(
                "INSERT INTO payment (payment_order_id, payment_key, amount) VALUES (?, ?, ?)",
                paymentOrderId,
                "payment-key-confirmed",
                response.amount()
        );

        ReservationResponse reservation = reservationService.findAllByName("결제확정조회").get(0);

        assertThat(reservation.paymentStatus()).isEqualTo("CONFIRMED");
        assertThat(reservation.orderId()).isEqualTo(response.orderId());
        assertThat(reservation.paymentKey()).isEqualTo("payment-key-confirmed");
        assertThat(reservation.amount()).isEqualTo(10000L);
    }

    @DisplayName("결과가 불명확한 예약은 결제 실패가 아닌 확인 필요 상태로 조회된다.")
    @Test
    void 결제_확인_필요_예약_조회() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("결제확인필요", now.toLocalDate().plusDays(73), 2L, 1L);
        ReservationResponse response = reservationService.save(now, request);

        jdbcTemplate.update("UPDATE payment_order SET payment_status = 'UNKNOWN' WHERE order_id = ?", response.orderId());

        ReservationResponse reservation = reservationService.findAllByName("결제확인필요").get(0);

        assertThat(reservation.paymentStatus()).isEqualTo("UNKNOWN");
        assertThat(reservation.paymentKey()).isNull();
        assertThat(reservation.orderId()).isEqualTo(response.orderId());
        assertThat(reservation.amount()).isEqualTo(10000L);
    }

    @DisplayName("예약 대기이면 결제 주문 정보가 응답에 포함되지 않는다.")
    @Test
    void 예약_대기시_결제_주문_정보_응답_제외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(71);
        ReservationRequest firstRequest = new ReservationRequest("김철수", reservationDate, 2L, 1L);
        ReservationRequest secondRequest = new ReservationRequest("이영희", reservationDate, 2L, 1L);

        reservationService.save(now, firstRequest);
        ReservationResponse waitingResponse = reservationService.save(now.plusSeconds(1), secondRequest);

        assertThat(waitingResponse.order()).isEqualTo(1);
        assertThat(waitingResponse.orderId()).isNull();
        assertThat(waitingResponse.paymentStatus()).isNull();
        assertThat(waitingResponse.paymentKey()).isNull();
        assertThat(waitingResponse.amount()).isNull();
    }

    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    @Test
    void 같은_사용자_중복_대기_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(2);
        ReservationRequest request = new ReservationRequest("브브브", reservationDate, 2L, 1L);

        reservationService.save(now, request);

        assertThatThrownBy(() -> reservationService.save(now.plusSeconds(1), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTS_RESERVATION.getMessage());
    }

    @DisplayName("사용자는 본인의 예약을 취소할 수 있고 취소 상태로 조회된다.")
    @Test
    void 본인_예약_취소_테스트() {
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest request = new ReservationRequest("김철수", now.toLocalDate().plusDays(2), 2L, 1L);
        ReservationResponse response = reservationService.save(now, request);

        reservationService.delete(now, response.reservationId(), "김철수");

        List<ReservationResponse> reservations = reservationService.findAllByName("김철수");
        assertThat(reservations)
                .anySatisfy(reservation -> {
                    assertThat(reservation.reservationId()).isEqualTo(response.reservationId());
                    assertThat(reservation.status()).isEqualTo("CANCELED");
                });
    }

    @DisplayName("결제 대기 예약을 취소하면 다음 대기자가 결제 대기로 승격되고 결제 주문 정보가 조회된다.")
    @Test
    void 결제_대기_취소시_다음_대기자_결제_대기_승격() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(80);
        ReservationRequest firstRequest = new ReservationRequest("취소승격1", reservationDate, 2L, 1L);
        ReservationRequest waitingRequest = new ReservationRequest("취소승격2", reservationDate, 2L, 1L);

        ReservationResponse firstResponse = reservationService.save(now, firstRequest);
        reservationService.save(now.plusSeconds(1), waitingRequest);

        reservationService.delete(now.plusSeconds(2), firstResponse.reservationId(), "취소승격1");

        ReservationResponse promoted = reservationService.findAllByName("취소승격2").get(0);
        assertThat(promoted.status()).isEqualTo("PAYMENT_PENDING");
        assertThat(promoted.order()).isZero();
        assertThat(promoted.orderId()).matches("[A-Za-z0-9_-]{6,64}");
        assertThat(promoted.paymentStatus()).isEqualTo("PENDING");
        assertThat(promoted.amount()).isEqualTo(10000L);
    }

    @DisplayName("결제 대기 예약을 수정하면 기존 슬롯의 다음 대기자가 결제 대기로 승격되고 결제 주문 정보가 조회된다.")
    @Test
    void 결제_대기_수정시_다음_대기자_결제_대기_승격() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(81);
        ReservationRequest firstRequest = new ReservationRequest("수정승격1", reservationDate, 2L, 1L);
        ReservationRequest waitingRequest = new ReservationRequest("수정승격2", reservationDate, 2L, 1L);
        ReservationRequest updateRequest = new ReservationRequest("수정승격1", reservationDate, 3L, 1L);

        ReservationResponse firstResponse = reservationService.save(now, firstRequest);
        reservationService.save(now.plusSeconds(1), waitingRequest);

        reservationService.update(firstResponse.reservationId(), now.plusSeconds(2), updateRequest);

        ReservationResponse promoted = reservationService.findAllByName("수정승격2").get(0);
        assertThat(promoted.status()).isEqualTo("PAYMENT_PENDING");
        assertThat(promoted.order()).isZero();
        assertThat(promoted.orderId()).matches("[A-Za-z0-9_-]{6,64}");
        assertThat(promoted.paymentStatus()).isEqualTo("PENDING");
        assertThat(promoted.amount()).isEqualTo(10000L);
    }

    @DisplayName("본인 예약이 아니면 예약 대기를 취소할 수 없다.")
    @Test
    void 본인_예약이_아니면_예약_대기_취소_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(2);
        ReservationRequest ownerRequest = new ReservationRequest("김철수", reservationDate, 2L, 1L);
        ReservationRequest waitingRequest = new ReservationRequest("이영희", reservationDate, 2L, 1L);

        reservationService.save(now, ownerRequest);
        ReservationResponse waitingResponse = reservationService.save(now.plusSeconds(1), waitingRequest);

        assertThatThrownBy(() -> reservationService.delete(now, waitingResponse.reservationId(), "김철수"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COMMON_UNAUTHORIZED.getMessage());
    }

    @DisplayName("수정하려는 날짜/시간에 예약이 없으면 예약 수정된다.")
    @Test
    void 예약_수정_정상_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(30);
        ReservationRequest request = new ReservationRequest("김철수", reservationDate, 2L, 1L);
        ReservationRequest updateRequest = new ReservationRequest("김철수", reservationDate, 3L, 1L);

        ReservationResponse response = reservationService.save(now, request);

        assertThatCode(() -> reservationService.update(response.reservationId(), now, updateRequest))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 수정 시에도 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    @Test
    void 예약_수정_같은_사용자_중복_대기_예외_테스트() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = now.toLocalDate().plusDays(2);
        ReservationRequest firstRequest = new ReservationRequest("김철수", reservationDate, 2L, 1L);
        ReservationRequest secondRequest = new ReservationRequest("김철수", reservationDate, 3L, 1L);
        ReservationRequest updateRequest = new ReservationRequest("김철수", reservationDate, 2L, 1L);

        reservationService.save(now, firstRequest);
        ReservationResponse secondResponse = reservationService.save(now.plusSeconds(1), secondRequest);

        assertThatThrownBy(() -> reservationService.update(secondResponse.reservationId(), now, updateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTS_RESERVATION.getMessage());
    }

    private Long findPaymentOrderId(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM payment_order WHERE order_id = ?",
                Long.class,
                orderId
        );
    }
}
