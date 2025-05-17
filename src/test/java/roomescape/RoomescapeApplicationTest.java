package roomescape;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import roomescape.infrastructure.auth.config.WebMvcConfiguration;

@SpringBootTest
@Import(WebMvcConfiguration.class)
class RoomescapeApplicationTest {

    @Test
    void contextLoads() {
    }
}
