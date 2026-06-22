package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.config.TestTimeConfig;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.dto.ReservationCreateResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.UserReservationResponse;
import roomescape.reservation.dto.UserReservationsResponse;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.parse("2026-08-05");

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private ReservationRequest request(String name, Long timeId, Long themeId) {
        return new ReservationRequest(name, FUTURE_DATE, timeId, themeId);
    }

    private Long saveTimeId() {
        return reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        ).getId();
    }

    private Long saveThemeId() {
        return themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        ).getId();
    }

    @Test
    void 비어있는_슬롯에_예약하면_결제_대기_상태가_된다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();

        // when
        ReservationCreateResponse response = reservationService.reserve(request("브라운", timeId, themeId));

        // then
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.payment()).isNotNull();
        assertThat(response.payment().orderId()).isNotBlank();
    }

    @Test
    void 이미_점유된_슬롯에_예약하면_대기로_등록된다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();
        reservationService.reserve(request("브라운", timeId, themeId));

        // when
        ReservationCreateResponse response = reservationService.reserve(request("어공", timeId, themeId));

        // then
        assertThat(response.status()).isEqualTo("WAITING");
        assertThat(response.payment()).isNull();
    }

    @Test
    void 점유_예약을_취소하면_첫_대기자가_확정으로_승급된다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();
        ReservationCreateResponse occupying = reservationService.reserve(request("브라운", timeId, themeId));
        ReservationCreateResponse waiting = reservationService.reserve(request("어공", timeId, themeId));

        // when
        reservationService.cancel(occupying.id());

        // then
        assertThat(reservationService.readById(waiting.id()).status()).isEqualTo("CONFIRMED");
    }

    @Test
    void 대기_목록_조회시_신청_순서대로_순번이_부여된다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();
        reservationService.reserve(request("브라운", timeId, themeId));   // 점유(결제 대기)
        reservationService.reserve(request("어공", timeId, themeId));     // 대기 1번
        reservationService.reserve(request("고구마", timeId, themeId));   // 대기 2번

        // when
        UserReservationsResponse response = reservationService.findUserReservations("고구마");

        // then
        List<UserReservationResponse> waitings = response.waitings();
        assertThat(waitings).hasSize(1);
        assertThat(waitings.get(0).waitingNumber()).isEqualTo(2L);
    }

    @Test
    void 이미_취소된_예약을_다시_취소하면_예외가_발생한다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();
        ReservationCreateResponse reservation = reservationService.reserve(request("브라운", timeId, themeId));
        reservationService.cancel(reservation.id());

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(reservation.id()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복_신청하면_예외가_발생한다() {
        // given
        Long timeId = saveTimeId();
        Long themeId = saveThemeId();
        reservationService.reserve(request("브라운", timeId, themeId));

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(request("브라운", timeId, themeId)))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_DUPLICATE);
    }

}
