package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.WaitingRequest;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("예약 대기 추가 테스트")
    void createWaitingTest() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(15, 40)));

        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );

        reservationService.createAdminReservation(adminReservationRequest);

        WaitingRequest waitingRequest = new WaitingRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId()
        );

        // when - then
        assertThatCode(
                () -> waitingService.createWaiting(waitingRequest, 2L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 삭제 테스트")
    void deleteWaitingTest() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(15, 40)));

        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId(),
                2L
        );

        reservationService.createAdminReservation(adminReservationRequest);

        WaitingRequest waitingRequest = new WaitingRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                reservationTime.getId()
        );

        waitingService.createWaiting(waitingRequest, 2L);

        // when
        waitingService.deleteWaiting(1L);

        // then
        assertThat(reservationService.getUserReservations(2L).size()).isEqualTo(1);
    }

}
