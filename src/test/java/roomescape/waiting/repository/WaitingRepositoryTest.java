package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.JpaRepositoryTest;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.model.WaitingWithRanking;

@JpaRepositoryTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("주어진 예약에 대해 가장 빨리 저장된 예약 대기 조회 성공")
    void getFirstByReservation() {
        // given
        List<Member> members = memberRepository.saveAll(MemberFixture.get(4));
        Reservation reservation = reservationRepository.save(new Reservation(
                members.get(0),
                LocalDate.parse("2099-04-11"),
                reservationTimeRepository.save(ReservationTimeFixture.getOne()),
                themeRepository.save(ThemeFixture.getOne())));

        Waiting waiting1 = waitingRepository.save(new Waiting(reservation, memberRepository.getById(1L)));
        Waiting waiting2 = waitingRepository.save(new Waiting(reservation, memberRepository.getById(2L)));
        Waiting waiting3 = waitingRepository.save(new Waiting(reservation, memberRepository.getById(3L)));

        // when & then
        assertThat(waitingRepository.getFirstByReservation(reservation)).isEqualTo(waiting1);
    }

    @Test
    @DisplayName("해당하는 회원의 대기 정보와 해당 대기보다 일찍 저장된 데이터의 갯수를 조회한다.")
    void findWaitingsWithRankByMemberId() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member, reservationTime, theme));
        Waiting waiting1 = waitingRepository.save(new Waiting(reservation, member));
        Waiting waiting2 = waitingRepository.save(new Waiting(reservation, member));

        // when
        List<WaitingWithRanking> waitingsWithRankByMember = waitingRepository.findWaitingsWithRankByMember(member);

        // then
        assertThat(waitingsWithRankByMember.get(0).waiting()).isEqualTo(waiting1);
        assertThat(waitingsWithRankByMember.get(0).ranking()).isZero();
        assertThat(waitingsWithRankByMember.get(1).waiting()).isEqualTo(waiting2);
        assertThat(waitingsWithRankByMember.get(1).ranking()).isEqualTo(1);
    }

    @Test
    @DisplayName("해당하는 회원, 예약과 동일한 예약 대기가 존재한다면, 참을 반환한다.")
    void existsByMemberIdAndReservationId() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member, reservationTime, theme));

        Waiting waiting = waitingRepository.save(new Waiting(reservation, member));

        // when & then
        assertThat(waitingRepository.existsByMemberIdAndReservationId(member.getId(), reservation.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("해당하는 회원, 예약과 동일한 예약 대기가 존재하지 않는다면, 거짓을 반환한다.")
    void existsByMemberIdAndReservationId_WhenNotExists() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member, reservationTime, theme));

        // when & then
        assertThat(waitingRepository.existsByMemberIdAndReservationId(member.getId(), reservation.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("주어진 예약에 대한 예약 대기가 존재할 경우, 참을 반환한다.")
    void existsByReservation() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member, reservationTime, theme));

        Waiting waiting = waitingRepository.save(new Waiting(reservation, member));

        // when & then
        assertThat(waitingRepository.existsByReservation(reservation)).isTrue();
    }

    @Test
    @DisplayName("주어진 예약에 대한 예약 대기가 존재하지 않을 경우, 거짓을 반환한다.")
    void existsByReservation_WhenNotExist() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation = reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member, reservationTime, theme));

        // when & then
        assertThat(waitingRepository.existsByReservation(reservation)).isFalse();
    }
}
