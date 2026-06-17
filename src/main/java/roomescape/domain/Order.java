package roomescape.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Order {

    private final Long id;

    private final String orderId;
    private final Long amount;
    private final String paymentKey;

    private final Long reservationId;

}
