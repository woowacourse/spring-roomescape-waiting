package roomescape.business.model.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.business.InvalidCreateArgumentException;

import static org.assertj.core.api.Assertions.*;

class ThemeTest {

    @Nested
    class 생성_테스트 {

        @Test
        void 이름은_최대_20자이다() {
            final String name = "dompoolemondompoolemon";

            assertThatThrownBy(() -> new Theme(name, "", ""))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }
    }
}
