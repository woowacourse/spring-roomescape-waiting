package roomescape.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class NetworkUncertain extends PaymentGatewayException {

    public NetworkUncertain() {
        super("결제 결과를 확인할 수 없습니다. 결제 내역을 확인하거나 고객센터에 문의해 주세요.");
    }

    @Override
    public HttpStatusCode getStatus() {
        return HttpStatus.GATEWAY_TIMEOUT;
    }

    @Override
    public String getCode() {
        return "NETWORK_UNCERTAIN";
    }
}