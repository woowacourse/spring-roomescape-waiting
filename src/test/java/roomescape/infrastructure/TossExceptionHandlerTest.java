package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentAuthException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;

class TossExceptionHandlerTest {

    static Stream<Arguments> codeToException() {
        return Stream.of(
                Arguments.of("ALREADY_PROCESSED_PAYMENT", AlreadyProcessedException.class),
                Arguments.of("DUPLICATED_ORDER_ID", InvalidPaymentRequestException.class),
                Arguments.of("NOT_FOUND_PAYMENT_SESSION", InvalidPaymentRequestException.class),
                Arguments.of("INVALID_REQUEST", InvalidPaymentRequestException.class),
                Arguments.of("UNAUTHORIZED_KEY", PaymentAuthException.class),
                Arguments.of("INVALID_API_KEY", PaymentAuthException.class),
                Arguments.of("REJECT_CARD_PAYMENT", CardRejectedException.class),
                Arguments.of("NOT_FOUND_PAYMENT", PaymentNotFoundException.class),
                Arguments.of("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentInternalException.class)
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("codeToException")
    void 정의된_코드는_대응하는_도메인_예외로_변환된다(String code, Class<? extends RuntimeException> expected) {
        RuntimeException result = TossExceptionHandler.toDomainException(code, "메시지");

        assertThat(result).isInstanceOf(expected);
        assertThat(result).hasMessage("메시지");
    }

    @Test
    void 정의되지_않은_코드는_기본_예외로_변환된다() {
        RuntimeException result = TossExceptionHandler.toDomainException("SOME_UNDEFINED_CODE", "메시지");

        assertThat(result).isInstanceOf(PaymentConfirmException.class);
    }
}
