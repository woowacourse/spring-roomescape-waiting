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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.WaitingResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
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
        member = memberRepository.save(new Member("sudal", "sudal@email.com", "sudal", Role.USER));
    }

    @DisplayName("예약 대기 생성 테스트")
    @Test
    void createWaiting() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12),
                reservationTime.getId(), theme.getId(), member.getId());

        WaitingResponse waitingResponse = waitingService.createWaiting(reservationRequest,
                reservationRequest.memberId());

        assertAll(
                () -> assertThat(waitingResponse.name()).isEqualTo(member.getName()),
                () -> assertThat(waitingResponse.date()).isEqualTo(LocalDate.of(2030, 12, 12)),
                () -> assertThat(waitingResponse.time()).isEqualTo(reservationTime.getStartAt()),
                () -> assertThat(waitingResponse.theme()).isEqualTo(theme.getName())
        );
    }

    @DisplayName("과거 시간을 예약 대기하는 경우 예외가 발생한다.")
    @Test
    void validatePastTime() {
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(10, 0)));

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(1999, 12, 12),
                reservationTime.getId(),
                theme.getId(), 1L);

        assertThatThrownBy(
                () -> waitingService.createWaiting(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PAST_TIME_SLOT_WAITING.getErrorMessage());
    }

    @DisplayName("모든 예약 대기 조회 테스트")
    @Test
    void findAllWaitings() {
        List<Waiting> waitings = List.of(
                new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme),
                new Waiting(member, LocalDate.of(2999, 12, 13), reservationTime, theme));

        waitingRepository.saveAll(waitings);

        List<WaitingResponse> allWaitings = waitingService.findAllWaitings();

        assertThat(allWaitings).hasSize(2);
    }

    @DisplayName("예약 대기 삭제 테스트")
    @Test
    void deleteWaiting() {
        Waiting waiting = waitingRepository.save(
                new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme));

        waitingService.deleteWaiting(waiting.getId());

        List<Waiting> allWaitings = waitingRepository.findAll();
        assertThat(allWaitings).isEmpty();
    }

    @DisplayName("순번을 포함한 예약 대기 조회 테스트")
    @Test
    void findAllWithRankByMember() {
        Member otherMember = memberRepository.save(new Member("runnerDuck", "runnerDuck@email.com", "runnerDuck", Role.USER));
        List<Waiting> waitings = List.of(
                new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme),
                new Waiting(otherMember, LocalDate.of(2999, 12, 12), reservationTime, theme));

        waitingRepository.saveAll(waitings);

        List<WaitingWithRank> allWithRankByMembers = waitingService.findAllWithRankByMember(otherMember);

        WaitingWithRank waitingWithRank = allWithRankByMembers.get(0);
        assertAll(
                () -> assertThat(waitingWithRank.getWaiting().getMember()).isEqualTo(otherMember),
                () -> assertThat(waitingWithRank.getRank()).isEqualTo(2L)
        );
    }

    @DisplayName("주어진 예약과 동일한 날짜, 시간, 테마의 예약 대기가 있다면 예약으로 전환한다.")
    @Test
    void convertFirstWaitingToReservation() {
        waitingRepository.save(new Waiting(member, LocalDate.of(2999, 12, 12), reservationTime, theme));
        Reservation reservation = reservationRepository.save(new Reservation(member, LocalDate.of(2999, 12, 12), reservationTime, theme));

        waitingService.convertFirstWaitingToReservation(reservation);

        List<Waiting> allWaitings = waitingRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();
        assertAll(
                () -> assertThat(allWaitings.isEmpty()),
                () -> assertThat(allReservations.get(0).getMember()).isEqualTo(member)
        );
    }
}
