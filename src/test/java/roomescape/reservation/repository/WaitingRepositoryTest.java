package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.vo.WaitingWithRank;

@DataJpaTest
class WaitingRepositoryTest {

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

    @Test
    @DisplayName("예약 대기와 테마, 날짜, 시간이 같고 가장 먼저 생긴 예약을 가져온다.")
    void findFirstByThemeIdAndDateAndReservationTimeStartAtTest() {
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member1 = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        final LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(member1, date, theme, reservationTime));
        final Waiting waiting = waitingRepository.save(new Waiting(member1, date, theme, reservationTime));
        waitingRepository.save(new Waiting(member2, date, theme, reservationTime));

        final Optional<Waiting> actual = waitingRepository.findFirstByThemeIdAndDateAndReservationTimeStartAt(
                theme.getId(), date, time);

        assertThat(actual).isPresent();
        assertEquals(waiting, actual.get());
    }

    @Test
    @DisplayName("멤버id를 통해 그의 예약 대기와 순번을 반환한다.")
    void findWaitingWithRanksByMemberIdTest() {
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member1 = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        final LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(member1, date, theme, reservationTime));
        waitingRepository.save(new Waiting(member1, date, theme, reservationTime));
        waitingRepository.save(new Waiting(member1, date.plusDays(1), theme, reservationTime));
        final Waiting waiting1 = waitingRepository.save(new Waiting(member2, date, theme, reservationTime));
        final Waiting waiting2 = waitingRepository.save(new Waiting(member2, date.plusDays(1), theme, reservationTime));

        final List<WaitingWithRank> actual = waitingRepository.findWaitingWithRanksByMemberId(
                member2.getId());

        assertThat(actual).containsExactly(
                new WaitingWithRank(waiting1, 2L),
                new WaitingWithRank(waiting2, 2L)
        );
    }
}
