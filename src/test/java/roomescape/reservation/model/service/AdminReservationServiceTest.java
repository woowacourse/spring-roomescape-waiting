package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.reservation.application.AdminReservationService;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AdminReservationServiceTest {

    @Autowired
    private AdminReservationService adminReservationService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @PersistenceContext
    EntityManager em;

    private ReservationTime time;
    private ReservationTheme theme;
    private Member member1;
    private Member member2;
    private Member member3;
    private LocalDate tomorrow = LocalDate.now();

    @BeforeEach
    void setUp() {
        time = ReservationTestFixture.getReservationTimeFixture();
        em.persist(time);

        theme = ReservationTestFixture.getReservationThemeFixture();
        em.persist(theme);

        member1 = ReservationTestFixture.createUser("웨이드", "w@naver.com", "1234");
        em.persist(member1);

        member2 = ReservationTestFixture.createUser("두리", "e@naver.com", "1234");
        em.persist(member2);

        member3 = ReservationTestFixture.createUser("누구", "r@naver.com", "1234");
        em.persist(member3);

        em.flush();
    }

    @DisplayName("대기자 중 가장 먼저 등록된 대기자를 예약으로 승격한다")
    @Test
    void promoteTheFirstWaiter() {
        Waiting waitingFirst = new Waiting(theme, tomorrow, time, member1);
        Waiting waitingSecond = new Waiting(theme, tomorrow, time, member2);
        waitingRepository.save(waitingFirst);
        waitingRepository.save(waitingSecond);

        Optional<Reservation> result = adminReservationService.promoteWaiting(theme, tomorrow,
            time);
        assertThat(result).isPresent();
        assertThat(result.get().getMember()).isEqualTo(member1);
        assertThat(waitingRepository.findAll()).hasSize(1);
        assertThat(waitingRepository.findAll().getFirst().getMember().getId()).isEqualTo(
            member2.getId());
    }

    @DisplayName("대기자가 없으면 optional을 반환한다")
    @Test
    void NoWaitingReturnsOptional() {
        assertThat(adminReservationService.promoteWaiting(theme, tomorrow, time)).isEmpty();
    }

    @DisplayName("예약을 삭제하면 첫번째 대기자를 자동 승격한다")
    @Test
    void deleteThanPromote() {
        Reservation reservation = Reservation.builder()
            .theme(theme)
            .date(tomorrow)
            .time(time)
            .member(member1)
            .build();
        reservationRepository.save(reservation);
        Waiting waitingFirst = new Waiting(theme, tomorrow, time, member2);
        Waiting waitingSecond = new Waiting(theme, tomorrow, time,
            member3);
        waitingRepository.save(waitingFirst);
        waitingRepository.save(waitingSecond);

        adminReservationService.delete(reservation.getId());

        assertThat(waitingRepository.findAll().getFirst()).isEqualTo(waitingSecond);
        Reservation promoted = em.createQuery("select r from Reservation r where r.member.id = :id",
                Reservation.class)
            .setParameter("id", member2.getId())
            .getSingleResult();
        assertThat(promoted).isNotNull();
        assertThat(promoted.getTheme()).isEqualTo(theme);
        assertThat(promoted.getDate()).isEqualTo(tomorrow);
        assertThat(promoted.getTime()).isEqualTo(time);
    }
}
