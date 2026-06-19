package roomescape.controller.client.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import roomescape.client.TossConfirmResultUnknownException;
import roomescape.client.TossConnectionException;
import roomescape.client.TossPaymentException;
import roomescape.service.PaymentService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {

    private final PaymentService paymentService;

    @GetMapping("/reserve")
    public String reserve() {
        return "forward:/reservation.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/search")
    public String search() {
        return "forward:/search.html";
    }

    @GetMapping("/payment/checkout")
    public String paymentCheckout(
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam String orderName,
            @RequestParam String clientKey,
            Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);
        model.addAttribute("clientKey", clientKey);
        return "payment/checkout";
    }

    @GetMapping("/payments/success")
    public String paymentsSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {
        var result = paymentService.confirm(paymentKey, orderId, amount);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("result", result);
        return "payment/success";
    }

    @ExceptionHandler(TossPaymentException.AlreadyProcessed.class)
    public String handleAlreadyProcessed(TossPaymentException.AlreadyProcessed e, WebRequest request, Model model) {
        String orderId = request.getParameter("orderId");
        // 이미 승인된 결제에 대한 중복 confirm 호출 - 실패가 아니라 "이미 성공한 결제"이므로 성공 화면과 구분해서 안내한다.
        log.info("[이미 처리된 결제] orderId={} message={}", orderId, e.getMessage());
        model.addAttribute("title", "✅ 이미 처리된 결제예요");
        model.addAttribute("code", "ALREADY_PROCESSED");
        model.addAttribute("message", "이미 정상적으로 처리된 결제입니다. 중복 결제가 아니니 안심하고 예약 내역을 확인해주세요.");
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }

    @ExceptionHandler(TossConfirmResultUnknownException.class)
    public String handleResultUnknown(TossConfirmResultUnknownException e, WebRequest request, Model model) {
        String orderId = request.getParameter("orderId");
        // 응답을 받지 못해 승인 여부를 모르는 상태 - "실패"로 단정하지 않고 확인을 안내한다.
        log.warn("[결제 승인 결과 확인 불가] orderId={} message={}", orderId, e.getMessage());
        model.addAttribute("title", "⏳ 결제 결과를 확인하지 못했어요");
        model.addAttribute("code", "CONFIRM_RESULT_UNKNOWN");
        model.addAttribute("message", e.getMessage());
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }

    @ExceptionHandler(TossConnectionException.class)
    public String handleConnectionFailed(TossConnectionException e, WebRequest request, Model model) {
        String orderId = request.getParameter("orderId");
        // 요청 자체가 토스에 도달하지 못한 경우 - 미승인이 확실하므로 실패로 안내한다.
        log.warn("[결제 서버 연결 실패] orderId={} message={}", orderId, e.getMessage());
        model.addAttribute("code", "CONNECTION_FAILED");
        model.addAttribute("message", e.getMessage());
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }

    @ExceptionHandler(Exception.class)
    public String handleConfirmFailed(Exception e, WebRequest request, Model model) {
        String orderId = request.getParameter("orderId");
        model.addAttribute("code", "CONFIRM_FAILED");
        model.addAttribute("message", e.getMessage());
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }

    @GetMapping("/payments/fail")
    public String paymentsFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId,
            Model model) {
        try {
            paymentService.cancel(orderId);
        } catch (Exception e) {
            log.warn("[결제 실패 정리 실패] orderId={} message={}", orderId, e.getMessage());
        }
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
