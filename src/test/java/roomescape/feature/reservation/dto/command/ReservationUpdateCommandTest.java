package roomescape.feature.reservation.dto.command;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.global.error.exception.GeneralException;

class ReservationUpdateCommandTest {

    private static final ReserverName VALID_NAME = new ReserverName("예약자");
    private static final LocalDate VALID_DATE = LocalDate.now().plusDays(1);
    private static final Long VALID_TIME_ID = 1L;
    private static final Long VALID_THEME_ID = 1L;

    @Nested
    class 생성_시점에_값을_검증한다 {

        @Test
        void 모든_필드가_유효하면_생성에_성공한다() {
            assertThatCode(() -> new ReservationUpdateCommand(VALID_NAME, VALID_DATE, VALID_TIME_ID, VALID_THEME_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        void name이_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new ReservationUpdateCommand(null, VALID_DATE, VALID_TIME_ID, VALID_THEME_ID))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void date가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new ReservationUpdateCommand(VALID_NAME, null, VALID_TIME_ID, VALID_THEME_ID))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void timeId가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new ReservationUpdateCommand(VALID_NAME, VALID_DATE, null, VALID_THEME_ID))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void themeId가_null이면_예외를_던진다() {
            assertThatThrownBy(() -> new ReservationUpdateCommand(VALID_NAME, VALID_DATE, VALID_TIME_ID, null))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }
    }
}
