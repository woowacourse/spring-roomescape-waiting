package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.DuplicateReservationException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationWaitingRepository waitingRepository;

    private Member member;
    private ReservationTime time;
    private ReservationTime otherTime;
    private Theme theme;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.of("user1", "user1@test.com", "1234"));
        time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        otherTime = timeRepository.save(ReservationTime.restore(null, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        futureDate = LocalDate.now().plusDays(1);
    }

    @Test
    @DisplayName("예약을 생성하면 응답에 정보가 담기고 DB에 저장된다")
    void 예약_생성_시_응답과_DB에_저장된다() {
        ReservationRequest request = new ReservationRequest(futureDate, time.getId(), theme.getId());

        BookingResult result = reservationService.book(member, request);

        Reservation reservation = result.reservation();
        assertThat(result.isWaiting()).isFalse();
        assertThat(reservation.getId()).isNotNull().isPositive();
        assertThat(reservation.getMember().getName()).isEqualTo("user1");
        assertThat(reservation.getDate()).isEqualTo(futureDate);
        assertThat(reservation.getTheme().getName()).isEqualTo("테마A");
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, time.getId(), theme.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("본인이 이미 예약한 슬롯에 다시 신청하면 예외가 발생한다")
    void 본인이_예약한_슬롯에_다시_신청하면_예외가_발생한다() {
        ReservationRequest request = new ReservationRequest(futureDate, time.getId(), theme.getId());
        reservationService.book(member, request);

        assertThatThrownBy(() -> reservationService.book(member, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 예약한 슬롯입니다.");
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 다른 사람이 신청하면 대기로 생성된다")
    void 예약된_슬롯에_다른_사람이_신청하면_대기로_생성된다() {
        reservationService.book(member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        Member other = memberRepository.save(Member.of("other", "other@test.com", "1234"));

        BookingResult result = reservationService.book(
                other, new ReservationRequest(futureDate, time.getId(), theme.getId()));

        assertThat(result.isWaiting()).isTrue();
        assertThat(waitingRepository.findById(result.waiting().getId())).isPresent();
    }

    @Test
    @DisplayName("본인이 이미 대기 중인 슬롯에 다시 신청하면 예외가 발생한다")
    void 본인이_대기중인_슬롯에_다시_신청하면_예외가_발생한다() {
        reservationService.book(member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        Member other = memberRepository.save(Member.of("other", "other@test.com", "1234"));
        ReservationRequest request = new ReservationRequest(futureDate, time.getId(), theme.getId());
        reservationService.book(other, request);

        assertThatThrownBy(() -> reservationService.book(other, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 대기 중인 슬롯입니다.");
    }

    @Test
    @DisplayName("과거 시간으로는 예약을 생성할 수 없다")
    void 과거_시간으로는_예약을_생성할_수_없다() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().minusDays(1), time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.book(member, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 시간에는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("회원 ID로 본인의 예약 목록을 조회한다")
    void 회원ID로_본인_예약_목록을_조회한다() {
        reservationService.book(member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        reservationService.book(member, new ReservationRequest(futureDate, otherTime.getId(), theme.getId()));

        List<Reservation> reservations = reservationService.getReservationsByMember(member);

        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제하면 DB에서 제거된다")
    void 예약_삭제_시_DB에서_제거된다() {
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        reservationService.deleteReservation(reservationId, member);

        assertThat(reservationRepository.findById(reservationId)).isEmpty();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, time.getId(), theme.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("예약을 취소하면 같은 슬롯의 1순위 대기가 예약으로 승격된다")
    void 예약_취소_시_1순위_대기가_승격된다() {
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();
        Member waiter = memberRepository.save(Member.of("waiter", "waiter@test.com", "1234"));
        ReservationWaiting waiting = waitingRepository.save(
                ReservationWaiting.of(waiter, futureDate, time, theme));

        reservationService.deleteReservation(reservationId, member);

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
        List<Reservation> promoted = reservationRepository.findByMemberId(waiter.getId());
        assertThat(promoted).hasSize(1);
        assertThat(promoted.get(0).getDate()).isEqualTo(futureDate);
        assertThat(promoted.get(0).getTime().getId()).isEqualTo(time.getId());
    }

    @Test
    @DisplayName("대기가 없는 슬롯의 예약을 취소하면 승격 없이 삭제만 된다")
    void 대기_없는_예약_취소_시_삭제만_된다() {
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        reservationService.deleteReservation(reservationId, member);

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, time.getId(), theme.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 지난_예약은_취소할_수_없다() {
        Reservation past = reservationRepository.save(
                Reservation.restore(null, member, LocalDate.now().minusDays(1), time, theme));

        assertThatThrownBy(() -> reservationService.deleteReservation(past.getId(), member))
                .isInstanceOf(PastTimeCancelException.class);
    }

    @Test
    @DisplayName("다른 사람의 예약은 삭제할 수 없다")
    void 타인의_예약은_삭제할_수_없다() {
        Member other = memberRepository.save(Member.of("user2", "user2@test.com", "1234"));
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId, other))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회하면 예외가 발생한다")
    void 존재하지_않는_예약_조회_시_예외가_발생한다() {
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();
        reservationService.deleteReservation(reservationId, member);

        assertThatThrownBy(() -> reservationService.getById(reservationId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("기준 시각 이전에 생성된 미결제 예약은 만료되고 1순위 대기가 승격된다")
    void 만료된_미결제_예약은_삭제되고_대기가_승격된다() {
        Long pendingId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();
        Member waiter = memberRepository.save(Member.of("waiter", "waiter@test.com", "1234"));
        ReservationWaiting waiting = waitingRepository.save(ReservationWaiting.of(waiter, futureDate, time, theme));

        reservationService.expirePendingCreatedBefore(LocalDateTime.now().plusDays(1));

        assertThat(reservationRepository.findById(pendingId)).isEmpty();
        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
        assertThat(reservationRepository.findByMemberId(waiter.getId())).hasSize(1);
    }

    @Test
    @DisplayName("기준 시각 이후에 생성된 미결제 예약은 만료되지 않는다")
    void 기준_시각_이후_미결제_예약은_만료되지_않는다() {
        Long pendingId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        reservationService.expirePendingCreatedBefore(LocalDateTime.now().minusDays(1));

        assertThat(reservationRepository.findById(pendingId)).isPresent();
    }

    @Test
    @DisplayName("예약 날짜·시간을 변경하면 응답과 DB에 반영된다")
    void 예약_변경_시_응답과_DB에_반영된다() {
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();
        LocalDate newDate = futureDate.plusDays(1);

        Reservation updated = reservationService.updateReservation(
                reservationId, member, new ReservationUpdateRequest(newDate, otherTime.getId()));

        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime().getId()).isEqualTo(otherTime.getId());
        Reservation found = reservationRepository.findById(reservationId).get();
        assertThat(found.getDate()).isEqualTo(newDate);
        assertThat(found.getTime().getId()).isEqualTo(otherTime.getId());
    }

    @Test
    @DisplayName("변경하려는 슬롯이 이미 예약되어 있으면 예외가 발생한다")
    void 변경_대상_슬롯이_이미_예약되면_예외가_발생한다() {
        reservationService.book(member, new ReservationRequest(futureDate, otherTime.getId(), theme.getId()));
        Long targetId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        assertThatThrownBy(() -> reservationService.updateReservation(
                targetId, member, new ReservationUpdateRequest(futureDate, otherTime.getId())))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("다른 사람의 예약은 변경할 수 없다")
    void 타인의_예약은_변경할_수_없다() {
        Member other = memberRepository.save(Member.of("user2", "user2@test.com", "1234"));
        Long reservationId = reservationService.book(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId())).reservation().getId();

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId, other, new ReservationUpdateRequest(futureDate, otherTime.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void 지난_예약은_변경할_수_없다() {
        Reservation past = reservationRepository.save(
                Reservation.restore(null, member, LocalDate.now().minusDays(1), time, theme));

        assertThatThrownBy(() -> reservationService.updateReservation(
                past.getId(), member, new ReservationUpdateRequest(futureDate, otherTime.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 예약은 변경할 수 없습니다.");
    }
}
