package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private Member member;
    private Member reserver;
    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.of("user1", "user1@test.com", "1234"));
        reserver = memberRepository.save(Member.of("user2", "user2@test.com", "1234"));
        time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        futureDate = LocalDate.now().plusDays(1);
    }

    private void reserveByOther() {
        reservationRepository.save(Reservation.restore(null, reserver, futureDate, time, theme));
    }

    private ReservationWaitingRequest request() {
        return new ReservationWaitingRequest(futureDate, time.getId(), theme.getId());
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 대기를 신청하면 응답에 정보가 담기고 DB에 저장된다")
    void 대기_신청_시_응답과_DB에_저장된다() {
        reserveByOther();

        ReservationWaitingResponse response = reservationWaitingService.createWaiting(member, request());

        assertThat(response.id()).isNotNull().isPositive();
        assertThat(response.memberName()).isEqualTo("user1");
        assertThat(waitingRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("예약이 없는 슬롯에는 대기를 신청할 수 없다")
    void 예약이_없는_슬롯에는_대기할_수_없다() {
        assertThatThrownBy(() -> reservationWaitingService.createWaiting(member, request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약이 없는 슬롯에는 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("본인이 이미 예약한 슬롯에는 대기를 신청할 수 없다")
    void 본인이_예약한_슬롯에는_대기할_수_없다() {
        reservationRepository.save(Reservation.restore(null, member, futureDate, time, theme));

        assertThatThrownBy(() -> reservationWaitingService.createWaiting(member, request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 예약한 슬롯에는 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다")
    void 같은_슬롯에_중복_대기하면_예외가_발생한다() {
        reserveByOther();
        reservationWaitingService.createWaiting(member, request());

        assertThatThrownBy(() -> reservationWaitingService.createWaiting(member, request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("같은 슬롯에 중복 대기할 수 없습니다.");
    }

    @Test
    @DisplayName("대기를 삭제하면 DB에서 제거된다")
    void 대기_삭제_시_DB에서_제거된다() {
        reserveByOther();
        ReservationWaitingResponse response = reservationWaitingService.createWaiting(member, request());

        reservationWaitingService.deleteWaiting(response.id(), member.getId());

        assertThat(waitingRepository.findById(response.id())).isEmpty();
    }

    @Test
    @DisplayName("다른 사람의 대기는 삭제할 수 없다")
    void 타인의_대기는_삭제할_수_없다() {
        reserveByOther();
        ReservationWaitingResponse response = reservationWaitingService.createWaiting(member, request());

        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(response.id(), reserver.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("회원 ID로 대기 목록을 순번과 함께 조회한다")
    void 회원ID로_대기_목록을_순번과_함께_조회한다() {
        reserveByOther();
        reservationWaitingService.createWaiting(member, request());

        assertThat(reservationWaitingService.getWaitingByMemberId(member.getId()))
                .hasSize(1)
                .allSatisfy(it -> assertThat(it.turn()).isEqualTo(1L));
    }

    @Test
    @DisplayName("먼저 신청한 대기가 더 빠른 순번을 가진다")
    void 먼저_신청한_대기가_더_빠른_순번을_가진다() {
        reserveByOther();
        Member member3 = memberRepository.save(Member.of("user3", "user3@test.com", "1234"));
        ReservationWaitingResponse first = reservationWaitingService.createWaiting(member, request());
        ReservationWaitingResponse second = reservationWaitingService.createWaiting(member3, request());

        assertThat(reservationWaitingService.getWaitingByMemberId(member.getId()).get(0).turn()).isEqualTo(1L);
        assertThat(reservationWaitingService.getWaitingByMemberId(member3.getId()).get(0).turn()).isEqualTo(2L);
    }
}
