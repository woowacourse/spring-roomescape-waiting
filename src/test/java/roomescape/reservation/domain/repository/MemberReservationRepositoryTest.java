package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.util.RepositoryTest;

@DisplayName("사용자 예약 레포지토리 테스트")
class MemberReservationRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberReservationRepository memberReservationRepository;

    @DisplayName("예약을 저장한다.")
    @Test
    void save() {
        //given & when
        ReservationTime time = timeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        Member member = memberRepository.save(getMemberChoco());

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.PENDING));

        //then
        assertAll(() -> assertThat(memberReservation.getId()).isNotNull(),
                () -> assertThat(memberReservation.getReservation()).isEqualTo(reservation),
                () -> assertThat(memberReservation.getMember()).isEqualTo(member),
                () -> assertThat(memberReservation.getReservationStatus()).isEqualTo(ReservationStatus.PENDING)
        );
    }
}
