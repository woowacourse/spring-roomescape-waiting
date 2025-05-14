package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.reservation.application.dto.ReservationTimeRequest;
import roomescape.reservation.application.dto.ReservationTimeResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    void 모든_예약시간을_조회한다() {
        // given

        // when & then
        assertThat(reservationTimeService.findAll()).hasSize(6);
    }

    @Test
    void 예약시간을_추가한다() {
        // given
        final ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(22, 0));

        // when & then
        assertThat(reservationTimeService.add(request)).isEqualTo(new ReservationTimeResponse(7L, LocalTime.of(22, 0)));
    }

    @Test
    void 예약시간을_삭제한다() {
        // given
        final ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(22, 0));
        reservationTimeService.add(request);

        // when
        final Long id = 7L;

        // then
        assertThatCode(() -> reservationTimeService.deleteById(id)).doesNotThrowAnyException();
    }

    @Test
    void 사용중인_시간을_삭제하면_에러를_발생시킨다() {
        // given
        final Long id = 1L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이 시간의 예약이 존재합니다.");
    }
}
