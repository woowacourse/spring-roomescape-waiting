package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    private LoginMember loginMember;
    @Autowired
    private ThemeRepository themeRepository;

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
        ReservationResponseDto response = this.reservationService.saveReservation(request, this.loginMember);

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
        ReservationResponseDto saved = this.reservationService.saveReservation(request, this.loginMember);

        // when
        this.reservationService.cancelReservation(saved.id());

        // then
        List<ReservationResponseDto> reservations = this.reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간에 예약한다면 예외를 던진다")
    @Test
    void test4() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        this.reservationService.saveReservation(request, this.loginMember);
        ReservationRegisterDto savedRequest = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);

        // when && then
        assertThatThrownBy(
                () -> this.reservationService.saveReservation(savedRequest, this.loginMember))
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
                () -> this.reservationService.saveReservation(request, this.loginMember))
                .isInstanceOf(IllegalStateException.class);

    }

    @DisplayName("사용자가 예약한 예약 내역을 모두 가져온다")
    @Test
    void test6() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        Member savedMember = memberRepository.save(member);

        Reservation reservation = new Reservation(LocalDate.now().plusDays(1), savedReservationTime, savedTheme,
                savedMember, LocalDate.now());
        Reservation savedReservation = reservationRepository.save(reservation);

        LoginMember loginMember = new LoginMember(savedMember);

        //when
        List<MemberReservationResponseDto> response = reservationService.getReservationsOfMember(loginMember);

        List<MemberReservationResponseDto> comparedResponse = List.of(
                new MemberReservationResponseDto(savedReservation));

        //then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> assertThat(response).isEqualTo(comparedResponse)
        );
    }
}
