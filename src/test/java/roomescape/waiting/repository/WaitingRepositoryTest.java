package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
}
