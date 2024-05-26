package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.domain.WaitingWithRank;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
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
    private Member otherMember;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void insertReservation() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("happy", "hi", "abcd.html"));
        member = memberRepository.save(new Member("sudal", "sudal@email.com", "sudal", Role.USER));
        otherMember = memberRepository.save(new Member("runnerDuck", "runnerDuck@email.com", "runnerDuck", Role.USER));
        reservation1 = reservationRepository.save(
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 31), reservationTime, theme)));
        reservation2 = reservationRepository.save(
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 30), reservationTime, theme)));
    }

    @DisplayName("예약 대기 생성 테스트")
    @Test
    void createWaiting() {
        ReservationRequest reservationRequest = new ReservationRequest(
                reservation1.getSchedule().getDate(),
                reservation1.getSchedule().getTime().getId(),
                reservation1.getSchedule().getTheme().getId(),
                otherMember.getId());

        WaitingResponse waitingResponse = waitingService.createWaiting(reservationRequest, reservationRequest.memberId());

        assertAll(
                () -> assertThat(waitingResponse.name()).isEqualTo(otherMember.getName()),
                () -> assertThat(waitingResponse.date()).isEqualTo(reservation1.getSchedule().getDate()),
                () -> assertThat(waitingResponse.time()).isEqualTo(reservation1.getSchedule().getTime().getStartAt()),
                () -> assertThat(waitingResponse.theme()).isEqualTo(reservation1.getSchedule().getTheme().getName())
        );
    }

    @DisplayName("모든 예약 대기 조회 테스트")
    @Test
    void findAllWaitings() {
        List<Waiting> waitings = List.of(new Waiting(member, reservation1), new Waiting(member, reservation2));

        waitingRepository.saveAll(waitings);

        List<WaitingResponse> allWaitings = waitingService.findAllWaitings();

        assertThat(allWaitings).hasSize(2);
    }

    @DisplayName("예약 대기 삭제 테스트")
    @Test
    void deleteWaiting() {
        Waiting waiting = waitingRepository.save(new Waiting(member, reservation1));

        waitingService.deleteWaiting(waiting.getId());

        List<Waiting> allWaitings = waitingRepository.findAll();
        assertThat(allWaitings).isEmpty();
    }

    @DisplayName("순번을 포함한 예약 대기 조회 테스트")
    @Test
    void findAllWithRankByMember() {
        List<Waiting> waitings = List.of(
                new Waiting(member, reservation1),
                new Waiting(otherMember, reservation1)
        );

        waitingRepository.saveAll(waitings);

        List<WaitingWithRank> allWithRankByMembers = waitingService.findAllWithRankByMember(otherMember);

        WaitingWithRank waitingWithRank = allWithRankByMembers.get(0);
        assertAll(
                () -> assertThat(waitingWithRank.getWaiting().getMember()).isEqualTo(otherMember),
                () -> assertThat(waitingWithRank.getRank()).isEqualTo(2L)
        );
    }
}
