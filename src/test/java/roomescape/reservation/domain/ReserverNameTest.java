package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.member.domain.Name;
import roomescape.reservation.domain.exception.ReserverNameEmptyException;

public class ReserverNameTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void 예약자_이름은_비어있거나_null일_수_없다(String name) {
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(ReserverNameEmptyException.class);
    }
}
