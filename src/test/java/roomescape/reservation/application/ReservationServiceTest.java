package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.admin.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.time.application.ReservationTimeRepository;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.application.ThemeRepository;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("예약 추가 테스트")
    void createReservationTest() {
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

        // when - then
        assertThatCode(() -> reservationService.createReservation(adminReservationRequest)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 추가 시 예약 시간이 조회되지 않으면 예외가 발생한다")
    void createReservationExceptionTest() {
        // given
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                theme.getId(),
                1L,
                2L
        );

        // when - then
        assertThatThrownBy(() -> reservationService.createReservation(adminReservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("예약 시간 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약 추가 시 테마가 조회되지 않으면 예외가 발생한다")
    void createThemeExceptionTest() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(15, 40)));

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(
                LocalDate.of(2025, 8, 5),
                1L,
                reservationTime.getId(),
                2L
        );

        // when - then
        assertThatThrownBy(() -> reservationService.createReservation(adminReservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("테마 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약 전체 조회 테스트")
    void getReservationsTest() {
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
        reservationService.createReservation(adminReservationRequest);

        // when - then
        assertThat(reservationService.getReservations(null, null, null, null).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("유저 예약 조회 테스트")
    void getUserReservationsTest() {
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
        reservationService.createReservation(adminReservationRequest);

        // when - then
        assertAll(
                () -> assertThat(reservationService.getUserReservations(1L).size()).isEqualTo(0),
                () -> assertThat(reservationService.getUserReservations(2L).size()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("예약 삭제 테스트")
    void deleteReservationByUserTest() {
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
        ReservationResponse reservation = reservationService.createReservation(adminReservationRequest);

        // when
        reservationService.deleteReservationByAdmin(reservation.getId());

        // then
        assertThat(reservationService.getReservations(null, null, null, null).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("저장되어 있지 않은 id로 요청을 보내면 예외가 발생한다.")
    void deleteExceptionTest() {
        assertThatThrownBy(() -> reservationService.deleteReservationByAdmin(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제되어 있는 리소스입니다.");
    }
}
