package roomescape.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.TEST_DATE;

@ActiveProfiles("test")
@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("회원 ID로 예약을 찾을 수 있다.")
    void findByMemberId() {
        //given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);
        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);
        Reservation reservation = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme);
        entityManager.persist(reservation);

        // when
        List<Reservation> result = jpaReservationRepository.findByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation)
        );
    }

    @Test
    @DisplayName("예약 시간 ID를 통해 해당 시간에 대한 예약이 존재함을 확인할 수 있다.")
    void existsBySchedule_TimeId() {
        //given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);
        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);
        Reservation reservation = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme);
        entityManager.persist(reservation);

        //when
        boolean result = jpaReservationRepository.existsBySchedule_TimeId(time.getId());

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("날짜, 예약 시간 ID, 테마 ID가 같은 예약이 존재함을 확인할 수 있다.")
    void existsBySchedule() {
        //given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);
        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);
        Reservation reservation = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme);
        entityManager.persist(reservation);

        //when
        boolean result = jpaReservationRepository.existsBySchedule(new Schedule(TEST_DATE, time, theme));

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("테마 ID에 예약이 존재하는지 확인할 수 있다.")
    void existsBySchedule_ThemeId() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);
        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);
        Reservation reservation = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme);
        entityManager.persist(reservation);

        // when
        boolean result = jpaReservationRepository.existsBySchedule_ThemeId(theme.getId());

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("테마 ID와 날짜로 예약을 조회할 수 있다.")
    void findBySchedule_ThemeIdAndSchedule_Date() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);
        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);
        Reservation reservation = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme);
        entityManager.persist(reservation);

        // when
        List<Reservation> result = jpaReservationRepository.findBySchedule_ThemeIdAndSchedule_Date(theme.getId(), TEST_DATE);

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation)
        );
    }

    @Test
    @DisplayName("회원 ID로 예약 조회를 필터링할 수 있다.")
    void findReservationsInConditions1() {
        // given
        Member member1 = TestFixture.createMemberByName("member1");
        Member member2 = TestFixture.createMemberByName("member2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member1, TEST_DATE, time, theme);
        Reservation reservation2 = TestFixture.createDefaultReservation(member2, TEST_DATE, time, theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> result = jpaReservationRepository.findReservationsInConditions(member1.getId(), null, null, null);

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation1)
        );
    }

    @Test
    @DisplayName("테마 ID로 예약 조회를 필터링할 수 있다.")
    void findReservationsInConditions2() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);

        Theme theme1 = TestFixture.createThemeByName("theme1");
        entityManager.persist(theme1);
        Theme theme2 = TestFixture.createThemeByName("theme2");
        entityManager.persist(theme2);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme1);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, TEST_DATE, time, theme2);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> result = jpaReservationRepository.findReservationsInConditions(null, theme1.getId(), null, null);

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation1)
        );
    }

    @Test
    @DisplayName("시작 날짜로 예약 조회를 필터링할 수 있다.")
    void findReservationsInConditions3() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);

        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> result = jpaReservationRepository.findReservationsInConditions(null, null, LocalDate.of(2025, 1, 2), null);

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation2)
        );
    }

    @Test
    @DisplayName("종료 날짜로 예약 조회를 필터링할 수 있다.")
    void findReservationsInConditions4() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);

        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> result = jpaReservationRepository.findReservationsInConditions(null, null, null, LocalDate.of(2025, 1, 1));

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(reservation1)
        );
    }

    @Test
    @DisplayName("조건이 없으면 모든 예약을 조회한다.")
    void findReservationsInConditionsAll() {
        // given
        Member member = TestFixture.createDefaultMember();
        entityManager.persist(member);

        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> result = jpaReservationRepository.findReservationsInConditions(null, null, null, null);

        // then
        assertThat(result.size()).isEqualTo(2);
    }
}