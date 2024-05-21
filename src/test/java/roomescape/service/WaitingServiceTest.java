package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Test
    void checkOwn() {
        assertThatThrownBy(() -> waitingService.checkOwn(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
