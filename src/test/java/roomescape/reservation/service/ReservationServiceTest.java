package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
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
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
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

        ReservationResponse response = reservationService.createReservation(member, request);

        assertThat(response.id()).isNotNull().isPositive();
        assertThat(response.memberName()).isEqualTo("user1");
        assertThat(response.date()).isEqualTo(futureDate);
        assertThat(response.themeName()).isEqualTo("테마A");
        assertThat(reservationRepository.findById(response.id())).isPresent();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, time.getId(), theme.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("같은 슬롯에 중복 예약을 생성하면 예외가 발생한다")
    void 같은_슬롯에_중복_예약하면_예외가_발생한다() {
        ReservationRequest request = new ReservationRequest(futureDate, time.getId(), theme.getId());
        reservationService.createReservation(member, request);

        assertThatThrownBy(() -> reservationService.createReservation(member, request))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("과거 시간으로는 예약을 생성할 수 없다")
    void 과거_시간으로는_예약을_생성할_수_없다() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().minusDays(1), time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(member, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 시간에는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("회원 ID로 본인의 예약 목록을 조회한다")
    void 회원ID로_본인_예약_목록을_조회한다() {
        reservationService.createReservation(member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        reservationService.createReservation(member, new ReservationRequest(futureDate, otherTime.getId(), theme.getId()));

        List<ReservationResponse> reservations = reservationService.getReservationsByMemberId(member.getId());

        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제하면 DB에서 제거된다")
    void 예약_삭제_시_DB에서_제거된다() {
        ReservationResponse response = reservationService.createReservation(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId()));

        reservationService.deleteReservation(response.id(), member.getId());

        assertThat(reservationRepository.findById(response.id())).isEmpty();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, time.getId(), theme.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 지난_예약은_취소할_수_없다() {
        Reservation past = reservationRepository.save(
                Reservation.restore(null, member, LocalDate.now().minusDays(1), time, theme));

        assertThatThrownBy(() -> reservationService.deleteReservation(past.getId(), member.getId()))
                .isInstanceOf(PastTimeCancelException.class);
    }

    @Test
    @DisplayName("다른 사람의 예약은 삭제할 수 없다")
    void 타인의_예약은_삭제할_수_없다() {
        Member other = memberRepository.save(Member.of("user2", "user2@test.com", "1234"));
        ReservationResponse response = reservationService.createReservation(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationService.deleteReservation(response.id(), other.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회하면 예외가 발생한다")
    void 존재하지_않는_예약_조회_시_예외가_발생한다() {
        ReservationResponse response = reservationService.createReservation(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        reservationService.deleteReservation(response.id(), member.getId());

        assertThatThrownBy(() -> reservationService.getById(response.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("예약 날짜·시간을 변경하면 응답과 DB에 반영된다")
    void 예약_변경_시_응답과_DB에_반영된다() {
        ReservationResponse response = reservationService.createReservation(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        LocalDate newDate = futureDate.plusDays(1);

        ReservationResponse updated = reservationService.updateReservation(
                response.id(), new ReservationUpdateRequest(newDate, otherTime.getId()));

        assertThat(updated.date()).isEqualTo(newDate);
        assertThat(updated.time().id()).isEqualTo(otherTime.getId());
        Reservation found = reservationRepository.findById(response.id()).get();
        assertThat(found.getDate()).isEqualTo(newDate);
        assertThat(found.getTime().getId()).isEqualTo(otherTime.getId());
    }

    @Test
    @DisplayName("변경하려는 슬롯이 이미 예약되어 있으면 예외가 발생한다")
    void 변경_대상_슬롯이_이미_예약되면_예외가_발생한다() {
        reservationService.createReservation(member, new ReservationRequest(futureDate, otherTime.getId(), theme.getId()));
        ReservationResponse target = reservationService.createReservation(
                member, new ReservationRequest(futureDate, time.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationService.updateReservation(
                target.id(), new ReservationUpdateRequest(futureDate, otherTime.getId())))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void 지난_예약은_변경할_수_없다() {
        Reservation past = reservationRepository.save(
                Reservation.restore(null, member, LocalDate.now().minusDays(1), time, theme));

        assertThatThrownBy(() -> reservationService.updateReservation(
                past.getId(), new ReservationUpdateRequest(futureDate, otherTime.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 예약은 변경할 수 없습니다.");
    }
}
