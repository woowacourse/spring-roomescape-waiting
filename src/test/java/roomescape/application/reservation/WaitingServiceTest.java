package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;

@ServiceTest
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

    @Autowired
    private Clock clock;

    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private Member member;
    private Reservation reservation;

    @BeforeEach
    void setData() {
        date = LocalDate.of(2024, 1, 1);
        time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        theme = themeRepository.save(new Theme("themeName", "desc", "url"));
        member = memberRepository.save(MemberFixture.createMember("아루"));
        reservation = reservationRepository.save(new Reservation(member, date, time, theme, LocalDateTime.now(clock)));
    }

    @Test
    @DisplayName("정상적인 예약 대기 요청을 받아서 저장한다.")
    void saveWaiting() {
        Member newMember = memberRepository.save(MemberFixture.createMember("시소"));
        ReservationRequest reservationRequest = new ReservationRequest(newMember.getId(), date, time.getId(),
                theme.getId());

        waitingService.create(reservationRequest);

        List<Waiting> waitings = waitingRepository.findAll();
        assertThat(waitings).hasSize(1);
    }

    @Test
    @DisplayName("예약 대기를 하지 않아도 되는 상황이면 예외가 발생한다.")
    void saveWaitingExceptionWhenReservationNotExist() {
        Member member = memberRepository.save(MemberFixture.createMember("시소"));
        ReservationRequest reservationRequest = new ReservationRequest(member.getId(),
                LocalDate.of(2024, 1, 2), time.getId(), theme.getId());

        Assertions.assertThatThrownBy(() -> waitingService.create(reservationRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("예약과 동일한 예약 대기를 생성하면 예외가 발생한다.")
    void saveWaitingExceptionWhenReservationDuplicate() {
        ReservationRequest reservationRequest = new ReservationRequest(member.getId(), date, time.getId(),
                theme.getId());

        Assertions.assertThatThrownBy(() -> waitingService.create(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일한 예약은 생성이 불가합니다.");
    }

    @Test
    @DisplayName("기존 예약 대기와 동일한 예약 대기를 생성하면 예외가 발생한다.")
    void saveWaitingExceptionWhenWaitingDuplicate() {
        Member member = memberRepository.save(MemberFixture.createMember("시소"));
        ReservationRequest reservationRequest = new ReservationRequest(member.getId(), date, time.getId(),
                theme.getId());

        waitingService.create(reservationRequest);

        Assertions.assertThatThrownBy(() -> waitingService.create(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일한 예약 대기는 생성이 불가합니다.");
    }

    @Test
    @DisplayName("예약 대기 삭제 요청시 예약이 존재하면 예약 대기를 삭제한다.")
    void deleteWaiting() {
        Member newMember = memberRepository.save(MemberFixture.createMember("시소"));
        Waiting waiting = waitingRepository.save(new Waiting(reservation, newMember, LocalDateTime.now(clock)));

        waitingService.deleteByIdWhenAuthorization(newMember.getId(), waiting.getId());

        List<Waiting> waitings = waitingRepository.findAll();
        assertThat(waitings).isEmpty();
    }

    @Test
    @DisplayName("멤버의 예약 대기를 생성 순서에 따라 조회한다.")
    void readAllWaitingByMemberId() {
        Member member1 = memberRepository.save(MemberFixture.createMember("시소"));
        waitingRepository.save(new Waiting(reservation, member1, LocalDateTime.now(clock)));

        Member member2 = memberRepository.save(MemberFixture.createMember("호돌"));
        waitingRepository.save(new Waiting(reservation, member2, LocalDateTime.now(clock).minusHours(1)));

        List<ReservationStatusResponse> reservationResponses1 = waitingService.findAllByMemberId(member1.getId());
        List<ReservationStatusResponse> reservationResponses2 = waitingService.findAllByMemberId(member2.getId());
        assertAll(
                () -> assertThat(reservationResponses1.size()).isEqualTo(1),
                () -> assertThat(reservationResponses2.size()).isEqualTo(1),
                () -> assertThat(reservationResponses1.get(0).status()).contains("2번째"),
                () -> assertThat(reservationResponses2.get(0).status()).contains("1번째")
        );
    }

    @Test
    @DisplayName("모든 예약 대기를 조회한다.")
    void readAllWaiting() {
        Member member1 = memberRepository.save(MemberFixture.createMember("시소"));
        waitingRepository.save(new Waiting(reservation, member1, LocalDateTime.now(clock)));

        Member member2 = memberRepository.save(MemberFixture.createMember("호돌"));
        waitingRepository.save(new Waiting(reservation, member2, LocalDateTime.now(clock).minusHours(1)));

        List<ReservationResponse> responses = waitingService.findAll();
        assertThat(responses.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약 Id에 대한 가장 빠른 대기를 조회한다.")
    void findFirstByReservationId() {
        Member member1 = memberRepository.save(MemberFixture.createMember("시소"));
        waitingRepository.save(new Waiting(reservation, member1, LocalDateTime.now(clock)));

        Member member2 = memberRepository.save(MemberFixture.createMember("호돌"));
        Waiting firstWaiting = waitingRepository.save(
                new Waiting(reservation, member2, LocalDateTime.now(clock).minusHours(1)));

        Optional<Waiting> waiting = waitingService.findFirstByReservationId(reservation.getId());
        assertAll(
                () -> assertThat(waiting.isPresent()).isTrue(),
                () -> assertThat(waiting.get()).isEqualTo(firstWaiting)
        );
    }
}
