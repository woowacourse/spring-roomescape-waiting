package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationAndWaitingQueryServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationAndWaitingQueryService reservationAndWaitingQueryService;

    @BeforeEach
    void init() {
        databaseCleaner.cleanUp();
    }
    
    
    /*
    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        myReservationResponses.addAll(
                reservationRepository.findAllByMemberId(memberId).stream()
                        .map(MyReservationResponse::new)
                        .toList());
        myReservationResponses.addAll(
                waitingRepository.findWaitingWithRanksByMemberId(memberId).stream()
                        .map(MyReservationResponse::new)
                        .toList());

        return myReservationResponses;
    }
     */
    // findAllByMemberId 메서드 테스트 추가

    @Test
    @DisplayName("멤버id를 통해 그의 예약과 대기 및 순번을 반환한다.")
    void findWaitingWithRanksByMemberIdTest() {
        final LocalTime time = LocalTime.of(10, 0);
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member1 = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andole@woowa.com", "asd"));
        final LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(member1, date.plusDays(10), theme, reservationTime));
        reservationRepository.save(new Reservation(member1, date.plusDays(11), theme, reservationTime));
        waitingRepository.save(new Waiting(member2, date.plusDays(10), theme, reservationTime));
        waitingRepository.save(new Waiting(member2, date.plusDays(11), theme, reservationTime));

        final List<MyReservationResponse> actual = reservationAndWaitingQueryService.findAllByMemberId(member2.getId());

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getStatus()).isEqualTo(1);
    }
}
