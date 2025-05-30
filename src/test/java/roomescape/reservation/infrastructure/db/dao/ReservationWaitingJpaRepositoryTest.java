package roomescape.reservation.infrastructure.db.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.ReservationTestFixture.createAcceptWaiting;
import static roomescape.ReservationTestFixture.createPendingWaiting;
import static roomescape.ReservationTestFixture.getReservationThemeFixture;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;
import static roomescape.ReservationTestFixture.getUserFixture;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;
import roomescape.support.RepositoryTestSupport;

class ReservationWaitingJpaRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReservationWaitingJpaRepository reservationWaitingJpaRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTheme savedTheme;
    private ReservationTime savedTime;
    private Member savedMember;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
        savedMember = memberRepository.save(getUserFixture());
        testDate = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("대기가 없는 회원의 대기 목록은 빈 리스트를 반환한다")
    void findAllWithRankByMemberId_empty_list() {
        //given
        Long notWaitingMemberId = savedMember.getId();

        // when
        List<ReservationWaitingWithRank> result = reservationWaitingJpaRepository.findAllWithRankByMemberId(notWaitingMemberId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원이 첫 번째로 대기 등록한 경우 순위가 1이다")
    void findAllWithRankByMemberId_first_waiting_rank_one() {
        // given
        ReservationWaiting waiting = createPendingWaiting(testDate, savedTime, savedTheme, savedMember);
        reservationWaitingJpaRepository.save(waiting);

        // when
        List<ReservationWaitingWithRank> result = reservationWaitingJpaRepository.findAllWithRankByMemberId(
                savedMember.getId());

        // then
        ReservationWaitingWithRank waitingWithRank = result.get(0);

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(waitingWithRank.getReservationWaiting().getId()).isEqualTo(waiting.getId());
            softly.assertThat(waitingWithRank.getRankToInt()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("다른 사람들이 먼저 대기한 경우 순위가 올바르게 계산된다")
    void findAllWithRankByMemberId_correct_rank_calculation() {
        // given
        Member otherMember1 = getUserFixture();
        memberRepository.save(otherMember1);
        Member otherMember2 = getUserFixture();
        memberRepository.save(otherMember2);
        ReservationWaiting firstWaiting = createPendingWaiting(testDate, savedTime, savedTheme, otherMember1);
        reservationWaitingJpaRepository.save(firstWaiting);

        ReservationWaiting secondWaiting = createPendingWaiting(testDate, savedTime, savedTheme, otherMember2);
        reservationWaitingJpaRepository.save(secondWaiting);

        ReservationWaiting targetWaiting = createPendingWaiting(testDate, savedTime, savedTheme, savedMember);
        reservationWaitingJpaRepository.save(targetWaiting);

        // when
        List<ReservationWaitingWithRank> result = reservationWaitingJpaRepository.findAllWithRankByMemberId(
                savedMember.getId());

        // then
        ReservationWaitingWithRank waitingWithRank = result.get(0);

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(waitingWithRank.getReservationWaiting().getId()).isEqualTo(targetWaiting.getId());
            softly.assertThat(waitingWithRank.getRankToInt()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 대기는 순위 계산에 포함되지 않는다")
    void findAllWithRankByMemberId_only_pending_status_counted() {
        // given
        Member otherMember1 = getUserFixture();
        memberRepository.save(otherMember1);
        Member otherMember2 = getUserFixture();
        memberRepository.save(otherMember2);

        ReservationWaiting confirmedWaiting = createAcceptWaiting(testDate, savedTime, savedTheme, otherMember1);
        reservationWaitingJpaRepository.save(confirmedWaiting);

        // PENDING 상태의 대기
        ReservationWaiting pendingWaiting = createPendingWaiting(testDate, savedTime, savedTheme, otherMember2);
        reservationWaitingJpaRepository.save(pendingWaiting);

        // 대상 회원의 PENDING 대기
        ReservationWaiting targetWaiting = createPendingWaiting(testDate, savedTime, savedTheme, savedMember);
        reservationWaitingJpaRepository.save(targetWaiting);

        // when
        List<ReservationWaitingWithRank> result = reservationWaitingJpaRepository.findAllWithRankByMemberId(
                savedMember.getId());

        // then
        ReservationWaitingWithRank waitingWithRank = result.get(0);
        assertThat(result).hasSize(1);
        assertThat(waitingWithRank.getRankToInt()).isEqualTo(2);
    }
}
