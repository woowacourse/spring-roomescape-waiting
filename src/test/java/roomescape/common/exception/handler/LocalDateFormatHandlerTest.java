package roomescape.common.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LocalDateFormatHandlerTest {

    private final LocalDateFormatHandler handler = new LocalDateFormatHandler();

    @Nested
    class IsSupport {

        @Test
        @DisplayName("대상 타입이 LocalDate이면 true를 반환한다")
        void returnsTrueWhenLocalDate() {
            InvalidFormatException exception = mock(InvalidFormatException.class);
            given(exception.getTargetType()).willReturn((Class) LocalDate.class);

            assertThat(handler.isSupport(exception)).isTrue();
        }

        @Test
        @DisplayName("대상 타입이 LocalDate가 아니면 false를 반환한다")
        void returnsFalseWhenNotLocalDate() {
            InvalidFormatException exception = mock(InvalidFormatException.class);
            given(exception.getTargetType()).willReturn((Class) LocalTime.class);

            assertThat(handler.isSupport(exception)).isFalse();
        }
    }

    @Nested
    class Handle {

        @Test
        @DisplayName("날짜 형식 안내 메시지를 반환한다")
        void returnsDateFormatMessage() {
            InvalidFormatException exception = mock(InvalidFormatException.class);

            assertThat(handler.handle(exception)).contains("yyyy-MM-dd");
        }
    }

    @Nested
    class CanHandle {

        @Test
        @DisplayName("InvalidFormatException이면 true를 반환한다")
        void returnsTrueWhenInvalidFormatException() {
            InvalidFormatException exception = mock(InvalidFormatException.class);

            assertThat(handler.canHandle(exception)).isTrue();
        }

        @Test
        @DisplayName("InvalidFormatException이 아니면 false를 반환한다")
        void returnsFalseWhenNotInvalidFormatException() {
            assertThat(handler.canHandle(new RuntimeException())).isFalse();
        }
    }
}
