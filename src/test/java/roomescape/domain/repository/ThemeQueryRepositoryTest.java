package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DataJpaTest
class ThemeQueryRepositoryTest {

    @Autowired
    private ThemeQueryRepository themeQueryRepository;

    @DisplayName("존재하지 않는 테마 id로 조회시 예외가 발생한다.")
    @Test
    void getByIdExceptionTest() {
        assertThatCode(() -> themeQueryRepository.getById(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_THEME);
    }
}
