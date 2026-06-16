package roomescape.infrastructure;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentAuthException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;

public enum TossExceptionHandler {

    ALREADY_PROCESSED(AlreadyProcessedException::new, "ALREADY_PROCESSED_PAYMENT"),
    INVALID_REQUEST(InvalidPaymentRequestException::new,
            "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST"),
    UNAUTHORIZED(PaymentAuthException::new, "UNAUTHORIZED_KEY", "INVALID_API_KEY"),
    REJECT_CARD(CardRejectedException::new, "REJECT_CARD_PAYMENT"),
    NOT_FOUND(PaymentNotFoundException::new, "NOT_FOUND_PAYMENT"),
    INTERNAL(PaymentInternalException::new, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING"),
    ;

    private final Function<String, RuntimeException> exceptionFactory;
    private final List<String> codes;

    TossExceptionHandler(Function<String, RuntimeException> exceptionFactory, String... codes) {
        this.exceptionFactory = exceptionFactory;
        this.codes = List.of(codes);
    }

    public static RuntimeException toDomainException(String code, String message) {
        return Arrays.stream(values())
                .filter(handler -> handler.codes.contains(code))
                .findFirst()
                .<RuntimeException>map(handler -> handler.exceptionFactory.apply(message))
                .orElseGet(() -> new PaymentConfirmException(message));
    }
}
