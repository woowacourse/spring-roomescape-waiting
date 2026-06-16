package roomescape.order.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.payment.exception.PaymentAmountMismatchException;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Order {

    private final String orderId;
    private final Long reservationId;
    private final Long amount;

    public void validateAmountMatch(Long requestAmount) {
        if (!Objects.equals(amount, requestAmount)) {
            throw new PaymentAmountMismatchException(amount, requestAmount);
        }
    }

}
