package roomescape.service.dto;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.service.dto.request.ReservationSaveRequest;

class ReservationSaveRequestTest {

    @Test
    @DisplayName("이름이 정상 입력될 경우 성공한다.")
    void checkNameBlank_Success() {
        assertThatCode(() -> new ReservationSaveRequest(LocalDate.now(), 1L, 1L))
                .doesNotThrowAnyException();
    }
}
