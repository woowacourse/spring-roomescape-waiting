package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationStatus;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.IdNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    public static final Long UNAVAILABLE_ID = 999L;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 6, 1), UNAVAILABLE_ID, 1L);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 시간 정보를 찾을 수 없습니다.");
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 6, 1), 1L, UNAVAILABLE_ID);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 테마를 찾을 수 없습니다.");
    }

    @Test
    void 이미_지난_시간_날짜_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }

    @Test
    void 중복_없는_정상_예약() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 30), 1L, 1L);

        assertDoesNotThrow(() -> reservationService.save(request));
    }

    @Test
    void 이미_예약된_슬롯에_예약_시_대기_등록() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.of(2026, 6, 1), 1L, 1L);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("아나키", LocalDate.of(2026, 6, 1), 1L, 1L);
        ReservationResponse saved = reservationService.save(secondRequest);

        assertThat(saved.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 예약_변경_시_이미_예약이_존재하면_대기_불가() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.of(2026, 6, 1), 1L, 1L);
        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.of(2026, 6, 1), 2L, 1L);

        long id = reservationService.save(firstRequest).id();
        reservationService.save(secondRequest);

        UserReservationUpdateRequest updateRequest = new UserReservationUpdateRequest(LocalDate.of(2026, 6, 1), 2L, 1L);

        assertThatThrownBy(() -> reservationService.update(id, updateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 같은_사용자_중복_예약_및_대기_불가() {
        ReservationRequest firstRequest = new ReservationRequest("그해", LocalDate.of(2026, 6, 1), 1L, 1L);
        reservationService.save(firstRequest);

        ReservationRequest secondRequest = new ReservationRequest("그해", LocalDate.of(2026, 6, 1), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(secondRequest))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
