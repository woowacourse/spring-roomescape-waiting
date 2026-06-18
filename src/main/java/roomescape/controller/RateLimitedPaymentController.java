package roomescape.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rate Limit 의 보호를 받는 결제 승인 엔드포인트(학습용). 받은 값을 echo 해 항상 승인 응답을 돌려준다.
 */
@RestController
public class RateLimitedPaymentController {

    @PostMapping("/v1/payments/confirm")
    public Map<String, Object> confirm(@RequestBody(required = false) Map<String, Object> request) {
        var body = request != null ? request : Map.<String, Object>of();
        var response = new HashMap<String, Object>();
        response.put("paymentKey", body.getOrDefault("paymentKey", "gw-pk"));
        response.put("orderId", body.getOrDefault("orderId", "order"));
        response.put("status", "DONE");
        response.put("totalAmount", body.getOrDefault("amount", 0));
        return response;
    }

}
