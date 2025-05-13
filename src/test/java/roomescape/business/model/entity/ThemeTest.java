package roomescape.business.model.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.business.model.entity.Theme;
import roomescape.exception.business.InvalidCreateArgumentException;

class ThemeTest {

    @Nested
    class 생성_테스트 {

        @Test
        void 이름은_최대_20자이다() {
            final String name = "dompoolemondompoolemon";

            assertThatThrownBy(() -> Theme.create(name, "", ""))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }
    }
}
