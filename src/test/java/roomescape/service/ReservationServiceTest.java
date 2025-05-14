package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.model.Role;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    private LoginMember loginMember;

    @BeforeEach
    void setUp() {
        this.loginMember = new LoginMember(1L, "히로", "example@gmail.com", Role.ADMIN);
    }

    @DisplayName("예약을 정상적으로 저장한다.")
    @Test
    void test1() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationRegisterDto request = new ReservationRegisterDto(
                tomorrow.toString(),
                1L,
                1L
        );

        // when
        ReservationResponseDto response = reservationService.saveReservation(request, loginMember);

        // then
        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("히로"),
                () -> assertThat(response.date()).isEqualTo(tomorrow)
        );
    }

    @DisplayName("예약을 취소한다")
    @Test
    void test3() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        ReservationResponseDto saved = reservationService.saveReservation(request, loginMember);

        // when
        reservationService.cancelReservation(saved.id());

        // then
        List<ReservationResponseDto> reservations = reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간에 예약한다면 예외를 던진다")
    @Test
    void test4() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        reservationService.saveReservation(request, loginMember);
        ReservationRegisterDto savedRequest = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);

        // when && then
        assertThatThrownBy(
                () -> reservationService.saveReservation(savedRequest, loginMember))
                .isInstanceOf(DuplicatedException.class);
    }

    @DisplayName("당일 예약을 한다면 예외를 던진다")
    @Test
    void test5() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().toString(), 1L, 1L);
        // when && then
        assertThatThrownBy(
                () -> reservationService.saveReservation(request, loginMember))
                .isInstanceOf(IllegalStateException.class);
    }
}
