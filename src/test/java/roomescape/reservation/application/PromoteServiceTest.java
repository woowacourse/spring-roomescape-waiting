package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.MemberRepositoryAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.ReservationTimeRepositoryAdapter;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infrastructure.ThemeRepositoryAdapter;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.infrastructure.WaitingRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
        PromoteService.class,
        ReservationRepositoryAdapter.class,
        MemberRepositoryAdapter.class,
        ReservationTimeRepositoryAdapter.class,
        ThemeRepositoryAdapter.class,
        WaitingRepositoryAdapter.class
})
class PromoteServiceTest {

    @Autowired
    private PromoteService promoteService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("대기가 있는 경우 대기를 예약으로 승격")
    @Test
    void promoteWaiting_withWaitings() {
        // given
        Member reservationMember = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(reservationMember);

        Member waitingMember1 = MemberFixture.createMember("김진우", "jinu@example.com", "password456");
        memberRepository.save(waitingMember1);

        Member waitingMember2 = MemberFixture.createMember("홍길동", "hong@example.com", "password789");
        memberRepository.save(waitingMember2);

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        Reservation reservation = new Reservation(reservationMember, spec);
        reservationRepository.save(reservation);

        // Create two waitings
        Waiting waiting1 = new Waiting(waitingMember1, spec);
        waitingRepository.save(waiting1);

        Waiting waiting2 = new Waiting(waitingMember2, spec);
        waitingRepository.save(waiting2);

        // when
        promoteService.promoteWaiting(reservation);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(2);

        Reservation newReservation = reservations.stream()
                .filter(r -> !r.getId().equals(reservation.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(newReservation.getSpec()).isEqualTo(spec);

        // Verify that one of the waiting members was promoted
        assertThat(newReservation.getMember().getId())
                .isIn(waitingMember1.getId(), waitingMember2.getId());

        // Verify that one waiting was removed
        List<Waiting> remainingWaitings = waitingRepository.findAll();
        assertThat(remainingWaitings).hasSize(1);
    }
}
