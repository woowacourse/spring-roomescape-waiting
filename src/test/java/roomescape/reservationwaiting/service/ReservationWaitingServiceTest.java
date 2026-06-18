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
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;
    @Autowired
    private ReservationWaitingRepository waitingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private Member member;
    private Member other;
    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.of("user1", "user1@test.com", "1234"));
        other = memberRepository.save(Member.of("user2", "user2@test.com", "1234"));
        time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        futureDate = LocalDate.now().plusDays(1);
    }

    private ReservationWaiting saveWaiting(Member waiter) {
        return waitingRepository.save(ReservationWaiting.of(waiter, futureDate, time, theme));
    }

    @Test
    @DisplayName("대기를 삭제하면 DB에서 제거된다")
    void 대기_삭제_시_DB에서_제거된다() {
        ReservationWaiting waiting = saveWaiting(member);

        reservationWaitingService.deleteWaiting(waiting.getId(), member.getId());

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 사람의 대기는 삭제할 수 없다")
    void 타인의_대기는_삭제할_수_없다() {
        ReservationWaiting waiting = saveWaiting(member);

        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(waiting.getId(), other.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("회원 ID로 대기 목록을 순번과 함께 조회한다")
    void 회원ID로_대기_목록을_순번과_함께_조회한다() {
        saveWaiting(member);

        assertThat(reservationWaitingService.getWaitingByMemberId(member.getId()))
                .hasSize(1)
                .allSatisfy(it -> assertThat(it.turn()).isEqualTo(1L));
    }

    @Test
    @DisplayName("먼저 신청한 대기가 더 빠른 순번을 가진다")
    void 먼저_신청한_대기가_더_빠른_순번을_가진다() {
        saveWaiting(member);
        saveWaiting(other);

        assertThat(reservationWaitingService.getWaitingByMemberId(member.getId()).get(0).turn()).isEqualTo(1L);
        assertThat(reservationWaitingService.getWaitingByMemberId(other.getId()).get(0).turn()).isEqualTo(2L);
    }

    @Test
    @DisplayName("어드민은 전체 대기 목록을 조회한다")
    void 어드민은_전체_대기_목록을_조회한다() {
        saveWaiting(member);
        saveWaiting(other);

        assertThat(reservationWaitingService.getAllWaitings()).hasSize(2);
    }

    @Test
    @DisplayName("어드민은 소유자가 아니어도 대기를 취소할 수 있다")
    void 어드민은_소유자가_아니어도_대기를_취소할_수_있다() {
        ReservationWaiting waiting = saveWaiting(member);

        reservationWaitingService.deleteWaitingByAdmin(waiting.getId());

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("어드민이 존재하지 않는 대기를 취소하면 예외가 발생한다")
    void 어드민이_존재하지_않는_대기를_취소하면_예외() {
        assertThatThrownBy(() -> reservationWaitingService.deleteWaitingByAdmin(99999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 대기입니다.");
    }
}
