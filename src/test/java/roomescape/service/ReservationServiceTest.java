package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외() {
        Long timeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), timeId, 1L);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외() {
        Long themeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), 1L, themeId);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 이미_지난_시간_날짜_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().minusDays(1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }

    @Test
    void 중복_없는_정상_예약() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.now().plusDays(1), 1L, 1L);

        assertDoesNotThrow(() -> reservationService.save(request));
    }

    @Test
    void 이미_예약된_슬롯에_예약_시_대기_등록() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), 1L, 1L);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("아나키", LocalDate.now().plusDays(1), 1L, 1L);
        ReservationResponse saved = reservationService.save(secondRequest);

        assertThat(saved.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 예약_변경_시_이미_예약이_존재하면_대기_불가() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), 1L, 1L);
        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), 2L, 1L);

        long id = reservationService.save(firstRequest).id();
        reservationService.save(secondRequest);

        UserReservationUpdateRequest updateRequest = new UserReservationUpdateRequest(LocalDate.now().plusDays(1), 2L);

        assertThatThrownBy(() -> reservationService.update(id, updateRequest))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void 같은_사용자_중복_예약_및_대기_불가() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), 1L, 1L);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.now().plusDays(1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(secondRequest))
                .isInstanceOf(AlreadyExistsException.class);
    }
}
