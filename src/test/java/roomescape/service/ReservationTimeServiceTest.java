package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exception.ConflictException;
import roomescape.service.dto.command.ReservationTimeCommand;
import roomescape.service.dto.result.ReservationTimeResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("정상 시간을 생성하면 통과한다.")
    void 정상_시간_생성_테스트() {
        String time = "19:00";
        ReservationTimeCommand command = new ReservationTimeCommand(
                LocalTime.parse(time)
        );

        ReservationTimeResult saved = reservationTimeService.registerReservationTime(command);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.startAt()).isEqualTo(time);
    }

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제하면 에러가 발생한다.")
    void 예약_존재_시간_삭제_에러_테스트() {
        Long id = 1L;

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(id))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }
}
