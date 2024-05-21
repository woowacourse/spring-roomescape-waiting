package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DataJpaTest
class TimeQueryRepositoryTest {

    @Autowired
    private TimeQueryRepository timeQueryRepository;

    @DisplayName("존재하지 않는 시간으로 조회시 예외가 발생한다.")
    @Test
    void getByIdExceptionTest() {
        assertThatCode(() -> timeQueryRepository.getById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_TIME);
    }

}
