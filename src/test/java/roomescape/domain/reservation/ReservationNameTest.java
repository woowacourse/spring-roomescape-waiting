package roomescape.domain.reservation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import common.exception.RoomEscapeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationNameTest {
    private static final String UNDER_SIZE_NAME = "";
    private static final String OVER_SIZE_NAME = "---------------------";

    @ParameterizedTest
    @ValueSource(strings = {UNDER_SIZE_NAME, OVER_SIZE_NAME})
    void 이름이_범위를_벗어나면_예외가_발생한다(String name) {
        Assertions.assertThatThrownBy(() -> new ReservationName(name)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 양쪽_공백은_무시되어야_한다() {
        String name = "                               zeze                              ";
        Assertions.assertThat(new ReservationName(name)).isEqualTo(new ReservationName("zeze"));
    }

    @Test
    void null이_입력되면_예외가_발생한다() {
        String name = null;
        Assertions.assertThatThrownBy(() -> new ReservationName(name)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void 같은_이름인지_확인() {
        String source = "zeze";
        ReservationName zeze = new ReservationName(source);
        assertSoftly(softly -> {
            softly.assertThat(zeze.isSame(source)).isTrue();
            softly.assertThat(zeze.isSame("diff")).isFalse();
        });
    }
}
