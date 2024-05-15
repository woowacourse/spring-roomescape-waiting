package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Role;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.service.dto.request.MemberRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberService memberService;

    private ReservationTimeResponse reservationTime;
    private ThemeResponse theme;
    private MemberResponse member;


    @BeforeEach
    void insertReservation() {
        reservationTime = reservationTimeService.createReservationTime(new ReservationTimeRequest(LocalTime.of(10, 0)));
        theme = themeService.createTheme(new ThemeRequest("happy", "hi", "abcd.html"));
        member = memberService.createMember(new MemberRequest("sudal", "sudal@email.com", "sudal123", Role.ADMIN));
    }

    @DisplayName("예약 생성 테스트")
    @Test
    void createReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12),
                reservationTime.id(), theme.id(), member.id());

        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest,
                reservationRequest.memberId());

        assertAll(
                () -> assertThat(reservationResponse.name()).isEqualTo(member.name()),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2030, 12, 12)),
                () -> assertThat(reservationResponse.theme()).isEqualTo(theme),
                () -> assertThat(reservationResponse.member()).isEqualTo(member),
                () -> assertThat(reservationResponse.time()).isEqualTo(reservationTime)
        );
    }

    @DisplayName("예약시간이 없는 경우 예외가 발생한다.")
    @Test
    void reservationTimeIsNotExist() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), 1000L, theme.id(),
                1L);

        assertThatThrownBy(
                () -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.NOT_FOUND_RESERVATION_TIME.getErrorMessage());
    }

    @DisplayName("과거 시간을 예약하는 경우 예외가 발생한다.")
    @Test
    void validatePastTime() {
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(
                new ReservationTimeRequest(LocalTime.of(10, 0)));

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(1999, 12, 12), reservationTime.id(),
                theme.id(), 1L);

        assertThatThrownBy(
                () -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PAST_TIME_SLOT_RESERVATION.getErrorMessage());
    }


    @DisplayName("모든 예약 조회 테스트")
    @Test
    void findAllReservations() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2999, 12, 12), reservationTime.id(),
                theme.id(), member.id());
        reservationService.createReservation(reservationRequest, reservationRequest.memberId());

        List<ReservationResponse> reservations = reservationService.findAllReservations();

        assertThat(reservations).hasSize(1);
    }

    @DisplayName("예약 삭제 테스트")
    @Test
    void deleteReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), reservationTime.id(),
                theme.id(), member.id());
        ReservationResponse savedReservation = reservationService.createReservation(reservationRequest,
                reservationRequest.memberId());

        reservationService.deleteReservation(savedReservation.id());

        List<ReservationResponse> reservations = reservationService.findAllReservations();
        assertThat(reservations).isEmpty();
    }

    @DisplayName("사용자별 모든 예약 조회 테스트")
    @Test
    void findAllByMemberId() {
        ReservationTimeResponse reservationTime1 = reservationTimeService.createReservationTime(new ReservationTimeRequest(LocalTime.of(10, 0)));
        ReservationTimeResponse reservationTime2= reservationTimeService.createReservationTime(new ReservationTimeRequest(LocalTime.of(12, 0)));

        MemberResponse member1 = memberService.createMember(new MemberRequest("sudal", "sudal@email.com", "sudal123", Role.ADMIN));
        MemberResponse member2 = memberService.createMember(new MemberRequest("rush", "rush@email.com", "rush", Role.ADMIN));

        ReservationRequest reservationRequest1 = new ReservationRequest(LocalDate.of(2030, 12, 12), reservationTime1.id(), theme.id(),
                member1.id());
        reservationService.createReservation(reservationRequest1, reservationRequest1.memberId());
        ReservationRequest reservationRequest2 = new ReservationRequest(LocalDate.of(2030, 12, 12), reservationTime2.id(), theme.id(),
                member2.id());
        reservationService.createReservation(reservationRequest2, reservationRequest2.memberId());

        List<MyReservationResponse> reservations = reservationService.findAllByMemberId(member1.id());

        assertThat(reservations).hasSize(1);
    }
}
