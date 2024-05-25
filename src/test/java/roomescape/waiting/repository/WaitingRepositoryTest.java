package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;
import static roomescape.fixture.MemberFixture.MEMBER_DUCK;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.TimeFixture.TIME_1;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.test.RepositoryTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithOrder;

class WaitingRepositoryTest extends RepositoryTest {
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 대기 중 첫번째 예약 대기를 가져올 수 있다.")
    @Test
    void findFirstByReservation_idOrderByCreatedAtAscTest() {
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        LocalDate date = LocalDate.now();

        Reservation reservation = reservationRepository.save(new Reservation(MEMBER_BRI, date, time, theme));
        Waiting firstWaiting = waitingRepository.save(new Waiting(reservation, MEMBER_BROWN));
        Waiting secondWaiting = waitingRepository.save(new Waiting(reservation, MEMBER_DUCK));

        Waiting actual = waitingRepository.findFirstByReservation_idOrderByCreatedAtAsc(1L).get();

        assertThat(firstWaiting).isEqualTo(firstWaiting);
    }

    @DisplayName("나의 예약 대기들을 조회할 수 있다.")
    @Test
    void findByMember_idTest() {
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        LocalDate date = LocalDate.now();

        Reservation reservation1 = reservationRepository.save(new Reservation(MEMBER_BRI, date, time, theme));
        Reservation reservation2 = reservationRepository.save(new Reservation(MEMBER_BRI, date.plusDays(1), time, theme));

        Member waitingMember = MEMBER_BROWN;
        Waiting waiting1 = waitingRepository.save(new Waiting(reservation1, waitingMember));
        Waiting waiting2 = waitingRepository.save(new Waiting(reservation2, waitingMember));

        assertThat(waitingRepository.findByMember_idWithRank(waitingMember.getId()))
                .containsExactlyInAnyOrder(new WaitingWithOrder(waiting1, 1L), new WaitingWithOrder(waiting2, 1L));
    }

    @DisplayName("예약과 사용자 id가 일치하는 예약 대기를 찾을 수 있다.")
    @Test
    void existsByReservation_idAndMember_idTrueTest() {
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        LocalDate date = LocalDate.now();

        Reservation reservation = reservationRepository.save(new Reservation(MEMBER_BRI, date, time, theme));

        Member waitingMember = MEMBER_BROWN;
        waitingRepository.save(new Waiting(reservation, waitingMember));

        assertThat(waitingRepository.existsByReservation_idAndMember_id(reservation.getId(), waitingMember.getId()))
                .isTrue();
    }

    @DisplayName("예약과 사용자 id가 일치하는 예약 대기를 찾을 수 없다.")
    @Test
    void existsByReservation_idAndMember_idFalseTest() {
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        LocalDate date = LocalDate.now();

        Reservation reservation = reservationRepository.save(new Reservation(MEMBER_BRI, date, time, theme));

        assertThat(waitingRepository.existsByReservation_idAndMember_id(reservation.getId(), MEMBER_BROWN.getId()))
                .isFalse();
    }
}
