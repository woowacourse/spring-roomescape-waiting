package roomescape;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoomescapeApplicationTest {


    @Nested
    @DisplayName("contextLoads 메서드는")
    class ContextLoadsTest {


        @Test
        @DisplayName("contextLoads")
        void 성공() {
        }
    }
}
