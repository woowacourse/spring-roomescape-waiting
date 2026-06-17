package roomescape.payment.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.payment.application.OrderService;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.presentation.dto.OrderResponse;
import roomescape.payment.presentation.dto.PaymentHistoryResponse;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Value("${toss.client-key}")
    private String clientKey;

    @GetMapping("/checkout/{reservationId}")
    public String checkout(@PathVariable Long reservationId, Model model) {
        OrderInfo order = orderService.getOrder(reservationId);
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", order.orderId());
        model.addAttribute("orderName", order.reservation().themeName());
        model.addAttribute("amount", order.amount());
        return "checkout";
    }

    @ResponseBody
    @GetMapping("/{reservationId}")
    public ResponseEntity<OrderResponse> orders(@PathVariable Long reservationId) {
        OrderInfo order = orderService.getOrder(reservationId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/history")
    public String history(@RequestParam String name, Model model) {
        List<OrderInfo> orders = orderService.getOrdersByName(name);
        List<PaymentHistoryResponse> histories = orders.stream()
                .map(PaymentHistoryResponse::from)
                .toList();

        model.addAttribute("histories", histories);
        return "history";
    }
}
