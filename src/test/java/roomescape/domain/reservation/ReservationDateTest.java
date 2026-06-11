package roomescape.domain.reservation;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReservationDateTest {
    @Test
    void null을_입력받으면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ReservationDate(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void 유효한_요청이면_생성에_성공한다() {
        Assertions.assertThatNoException().isThrownBy(() -> new ReservationDate(LocalDate.now()));
    }
}
