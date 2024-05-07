package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.service.dto.request.MemberRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ReservationServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberService memberService;

    @DisplayName("예약 생성 테스트")
    @Test
    void createReservation() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        MemberRequest memberRequest = new MemberRequest("sudal", "sudal@email.com", "sudal123", Role.ADMIN);
        MemberResponse member = memberService.createMember(memberRequest);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), reservationTime.id(),
                theme.id(), member.id());
        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest, reservationRequest.memberId());

        assertAll(
                () -> assertThat(reservationResponse.member()).isEqualTo(member),
                () -> assertThat(reservationResponse.time()).isEqualTo(reservationTime)
        );
    }

    @DisplayName("예약시간이 없는 경우 예외가 발생한다.")
    @Test
    void reservationTimeIsNotExist() {
        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), 1L, theme.id(), 1L);
        assertThatThrownBy(() -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.NOT_FOUND_RESERVATION_TIME.getErrorMessage());
    }

    @DisplayName("과거 시간을 예약하는 경우 예외가 발생한다.")
    @Test
    void validatePastTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(1999, 12, 12), reservationTime.id(), theme.id(), 1L);
        assertThatThrownBy(() -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PAST_TIME_SLOT_RESERVATION.getErrorMessage());
    }


    @DisplayName("모든 예약 조회 테스트")
    @Test
    void findAllReservations() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        MemberRequest memberRequest = new MemberRequest("sudal", "sudal@email.com", "sudal123", Role.ADMIN);
        MemberResponse member = memberService.createMember(memberRequest);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2999, 12, 12), reservationTime.id(), theme.id(), member.id());
        reservationService.createReservation(reservationRequest, reservationRequest.memberId());


        List<ReservationResponse> reservations = reservationService.findAllReservations();

        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).name()).isEqualTo(member.name()),
                () -> assertThat(reservations.get(0).date()).isEqualTo(LocalDate.of(2999, 12, 12))
        );
    }

    @DisplayName("예약 삭제 테스트")
    @Test
    void deleteReservation() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        MemberRequest memberRequest = new MemberRequest("sudal", "sudal@email.com", "sudal123", Role.ADMIN);
        MemberResponse member = memberService.createMember(memberRequest);

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), reservationTime.id(), theme.id(), member.id());
        ReservationResponse savedReservation = reservationService.createReservation(reservationRequest, reservationRequest.memberId());

        reservationService.deleteReservation(savedReservation.id());

        List<ReservationResponse> reservations = reservationService.findAllReservations();

        assertThat(reservations).isEmpty();
    }
}
