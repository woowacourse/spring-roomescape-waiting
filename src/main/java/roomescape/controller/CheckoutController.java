package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.client.TossPaymentException;
import roomescape.payment.Payment;
import roomescape.payment.PaymentResult;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.PaymentService;
import roomescape.service.exception.PaymentAmountMismatchException;
import roomescape.service.exception.ResourceNotFoundException;

/**
 * 결제 흐름(주문 생성 → 위젯 인증 → 승인)을 처리하는 SSR(Thymeleaf) 컨트롤러.
 * 주문은 결제 '전에' checkout 에서 저장해 두고, successUrl 의 amount 검증 기준이 된다.
 */
@Controller
public class CheckoutController {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final PaymentService paymentService;
    private final String clientKey;

    public CheckoutController(
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository,
            PaymentService paymentService,
            @Value("${toss.client-key}") String clientKey
    ) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/checkout")
    public String checkout(@RequestParam Long reservationId, Model model) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약을 찾을 수 없습니다: reservationId=" + reservationId));
        Theme theme = themeRepository.findById(reservation.getTheme().getId())
                .orElseThrow(() -> new ResourceNotFoundException("테마를 찾을 수 없습니다: themeId=" + reservation.getTheme().getId()));

        Payment order = paymentService.createOrder(reservationId, theme.getPrice());

        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", order.orderId());
        model.addAttribute("orderName", theme.getName() + " 예약");
        model.addAttribute("amount", order.amount());
        return "checkout";
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            model.addAttribute("paymentKey", paymentKey);
            return "success";
        } catch (PaymentAmountMismatchException e) {
            // 위변조 차단. 결제 대기 상태로 남은 주문/예약을 failUrl 과 동일하게 정리한다.
            paymentService.cancelOrder(orderId);
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            // 승인 실패(카드 거절·키 오류 등). 결제 대기 주문/예약을 정리해 고아 데이터가 남지 않게 한다.
            paymentService.cancelOrder(orderId);
            return failView(model, e.getCode(), e.getMessage(), orderId);
        } catch (ResourceNotFoundException e) {
            // 주문을 찾을 수 없는 경우. 정리할 대상이 없으므로 안내만 한다.
            return failView(model, "ORDER_NOT_FOUND", e.getMessage(), orderId);
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        // 사용자 취소 시 orderId 가 없을 수 있다.
        if (orderId != null) {
            paymentService.cancelOrder(orderId);
        }
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}
