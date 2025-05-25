package roomescape.reservation.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.jpa.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.jpa.JpaReservationTimeRepository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.jpa.JpaScheduleRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.jpa.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaScheduleRepository jpaScheduleRepository;

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        member = jpaMemberRepository.save(new Member(null, "test", "test@test.com", MemberRole.USER, "testpassword"));
        theme = jpaThemeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        schedule = jpaScheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(1), time, theme));
    }

    @Test
    void 예약_생성_확인() {
        Reservation saved = jpaReservationRepository.save(new Reservation(null, schedule, member));
        assertThatCode(() -> jpaReservationRepository.findById(saved.getId())).doesNotThrowAnyException();
    }

    @Test
    void 특정_시간의_예약이_존재하는경우_True() {
        Reservation saved = jpaReservationRepository.save(new Reservation(null, schedule, member));

        assertThat(jpaReservationRepository.existsByScheduleTimeId(time.getId())).isTrue();
    }

    @Test
    void 특정_시간의_예약이_존재하는경우_False() {
        Reservation savedReservation = jpaReservationRepository.save(new Reservation(null, schedule, member));
        jpaReservationRepository.delete(savedReservation);
        assertThat(jpaReservationRepository.existsByScheduleTimeId(savedReservation.getId())).isFalse();
    }

    @Test
    void 멤버의_테마_및_방문기간_별_예약내역_확인() {
        List<Reservation> reservations = createReservations();
        List<Reservation> savedReservations = jpaReservationRepository.saveAll(reservations);
        List<Reservation> findReservations = jpaReservationRepository.findByMemberAndThemeAndDateBetween(
                theme.getId(),
                member.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        assertThat(findReservations).hasSize(3);
        assertThat(findReservations).containsAll(savedReservations);
    }

    @Test
    void 멤버의_예약_목록을_확인() {
        List<Reservation> savedReservations = jpaReservationRepository.saveAll(createReservations());
        List<Reservation> findReservations = jpaReservationRepository.findAllByMember(member);
        assertThat(findReservations).hasSize(3);
        assertThat(findReservations).containsAll(savedReservations);
    }

    private List<Reservation> createReservations() {
        Schedule schedule2 = jpaScheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(2), time, theme));
        Schedule schedule3 = jpaScheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(3), time, theme));
        return List.of(
                new Reservation(null, schedule, member),
                new Reservation(null, schedule2, member),
                new Reservation(null, schedule3, member)
        );
    }
}
