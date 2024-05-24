package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.global.handler.exception.CustomException;
import roomescape.global.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.reservation.dto.request.WaitingRequest;
import roomescape.service.member.dto.MemberResponse;
import roomescape.service.reservation.dto.response.ReservationTimeResponse;
import roomescape.service.reservation.dto.response.ThemeResponse;
import roomescape.service.reservation.dto.response.WaitingResponse;
import roomescape.service.reservation.WaitingService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;


    @BeforeEach
    void insertReservation() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("happy", "hi", "abcd.html"));
        member = memberRepository.save(new Member("sudal", "sudal@email.com", "password", Role.USER));
    }

    @DisplayName("예약 대기 테스트")
    @Test
    void createReservation() {
        WaitingRequest waitingRequest = new WaitingRequest(LocalDate.of(2999, 12, 12), theme.getId(), reservationTime.getId());
        WaitingResponse waitingResponse = waitingService.createWaiting(waitingRequest, member.getId());

        assertAll(
                () -> assertThat(waitingResponse.name()).isEqualTo(member.getName()),
                () -> assertThat(waitingResponse.date()).isEqualTo(LocalDate.of(2999, 12, 12)),
                () -> assertThat(waitingResponse.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(waitingResponse.member()).isEqualTo(MemberResponse.from(member)),
                () -> assertThat(waitingResponse.time()).isEqualTo(ReservationTimeResponse.from(reservationTime))
        );
    }

    @DisplayName("회원은 동일 날짜, 테마에 예약 존재시 예약 대기 불가능")
    @Test
    void validateMemberAlreadyReservedThemeOnDate() {
        reservationRepository.save(new Reservation(member, LocalDate.of(2999, 12, 12), reservationTime, theme));

        WaitingRequest waitingRequest = new WaitingRequest(LocalDate.of(2999, 12, 12), theme.getId(), reservationTime.getId());
        assertThatThrownBy(() -> waitingService.createWaiting(waitingRequest, member.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.DUPLICATE_RESERVATION.getErrorMessage());

    }

    @DisplayName("회원은 동일 날짜, 시간, 테마에 중복 예약 대기를 할 수 없다.")
    @Test
    void validateMemberWaitingAlreadyExist() {
        Waiting waiting = new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme);
        waitingRepository.save(waiting);

        WaitingRequest waitingRequest = new WaitingRequest(LocalDate.of(2999, 12, 12), theme.getId(), reservationTime.getId());
        assertThatThrownBy(() -> waitingService.createWaiting(waitingRequest, member.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.ALREADY_WAITING_EXIST.getErrorMessage());

    }

    @DisplayName("모든 예약 대기 조회 테스트")
    @Test
    void findAllWaitings() {
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Waiting waiting = new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme);
        Waiting waiting2 = new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime2, theme);

        waitingRepository.save(waiting);
        waitingRepository.save(waiting2);

        assertThat(waitingService.findAllWaitings()).hasSize(2);
    }

    @DisplayName("예약 삭제 테스트")
    @Test
    void deleteWaiting() {
        Waiting waiting = new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme);
        Waiting savedWaiting = waitingRepository.save(waiting);

        waitingService.deleteWaiting(savedWaiting.getId());
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }


}
