package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository timeRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    @DisplayName("테마와 시작 시간을 통해 예약 대기 목록을 조회한다.")
    void findByThemeIdAndStartAt() {
        // given
        Theme theme = themeRepository.save(new Theme("Theme 1", "Desc 1", "Thumb 1"));
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = timeRepository.save(new ReservationTime("10:00"));

        List<Member> members = List.of(
                memberRepository.save(new Member("seyang@test.com", "seyang", "Seyang")),
                memberRepository.save(new Member("alpha@test.com", "alpha", "Alpha")),
                memberRepository.save(new Member("beta@test.com", "beta", "Beta"))
        );
        Reservation reservation = reservationRepository.save(new Reservation(members.get(0), theme, date, time));
        LocalDateTime threeDaysAgo = date.atTime(time.getStartAt()).minusDays(3);
        LocalDateTime twoDaysAgo = date.atTime(time.getStartAt()).minusDays(2);
        List<Waiting> waitingList = List.of(
                waitingRepository.save(new Waiting(reservation, members.get(1), threeDaysAgo)),
                waitingRepository.save(new Waiting(reservation, members.get(2), twoDaysAgo))
        );

        // when
        List<Waiting> actual = waitingRepository.findByThemeIdAndStartAt(theme.getId(), date, time.getStartAt());

        // then
        assertThat(actual).containsExactlyElementsOf(waitingList);
    }

    @Test
    @DisplayName("특정 사용자의 예약 대기를 순위를 포함하여 조회한다.")
    void findWithRankByMemberEmail() {
        // given
        Theme theme = themeRepository.save(new Theme("Theme 1", "Desc 1", "Thumb 1"));
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = timeRepository.save(new ReservationTime("10:00"));

        List<Member> members = List.of(
                memberRepository.save(new Member("seyang@test.com", "seyang", "Seyang")),
                memberRepository.save(new Member("alpha@test.com", "alpha", "Alpha")),
                memberRepository.save(new Member("beta@test.com", "beta", "Beta"))
        );
        Reservation reservation = reservationRepository.save(new Reservation(members.get(0), theme, date, time));
        LocalDateTime threeDaysAgo = date.atTime(time.getStartAt()).minusDays(3);
        LocalDateTime twoDaysAgo = date.atTime(time.getStartAt()).minusDays(2);
        waitingRepository.save(new Waiting(reservation, members.get(1), threeDaysAgo));
        waitingRepository.save(new Waiting(reservation, members.get(2), twoDaysAgo));

        // when
        List<WaitingWithRank> actual = waitingRepository.findWithRankByMemberEmail(
                members.get(2).getEmail());

        // then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getRank()).isEqualTo(2);
    }
}
