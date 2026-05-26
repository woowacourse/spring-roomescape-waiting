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

class LocalTimeFormatHandlerTest {

    private final LocalTimeFormatHandler handler = new LocalTimeFormatHandler();

    @Nested
    class IsSupport {

        @Test
        @DisplayName("대상 타입이 LocalTime이면 true를 반환한다")
        void returnsTrueWhenLocalTime() {
            InvalidFormatException exception = mock(InvalidFormatException.class);
            given(exception.getTargetType()).willReturn((Class) LocalTime.class);

            assertThat(handler.isSupport(exception)).isTrue();
        }

        @Test
        @DisplayName("대상 타입이 LocalTime이 아니면 false를 반환한다")
        void returnsFalseWhenNotLocalTime() {
            InvalidFormatException exception = mock(InvalidFormatException.class);
            given(exception.getTargetType()).willReturn((Class) LocalDate.class);

            assertThat(handler.isSupport(exception)).isFalse();
        }
    }

    @Nested
    class Handle {

        @Test
        @DisplayName("시간 형식 안내 메시지를 반환한다")
        void returnsTimeFormatMessage() {
            InvalidFormatException exception = mock(InvalidFormatException.class);

            assertThat(handler.handle(exception)).contains("HH:mm");
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
