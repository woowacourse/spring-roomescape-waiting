package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.dto.ReservationRankResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final Member member1 = new Member("t11@t1.com", "t1", "재즈", "MEMBER");
    private final Member member2 = new Member("t22@t2.com", "t2", "영이", "MEMBER");

    private final LocalDate date1 = LocalDate.parse("2025-11-30");
    private final LocalDate date2 = LocalDate.parse("2025-12-01");

    private final ReservationTime time = new ReservationTime("10:00");

    private final Theme theme = new Theme("공포", "공포다!", "hi.jpg");

    private final Reservation reservation1 = new Reservation(member1, theme, date1, time, CONFIRMED);
    private final Reservation reservation2 = new Reservation(member2, theme, date2, time, CONFIRMED);
    private final Reservation reservation3 = new Reservation(member1, theme, date2, time, WAITING);

    @DisplayName("회원이 예약한 예약들의 정보와 대기 순위를 조회한다.")
    @Test
    void find_reservation_rank_response() throws InterruptedException {
        timeRepository.save(time);
        themeRepository.save(theme);
        memberRepository.save(member1);
        memberRepository.save(member2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        ReservationRankResponse response1 = new ReservationRankResponse(1L, "공포", date1, time.getStartAt(), CONFIRMED,
                1);
        ReservationRankResponse response2 = new ReservationRankResponse(3L, "공포", date2, time.getStartAt(), WAITING,
                2);

        List<ReservationRankResponse> actual = reservationRepository.findReservationRankByMember(member1);

        assertThat(actual).usingRecursiveComparison().isEqualTo(List.of(response1, response2));
    }
}
