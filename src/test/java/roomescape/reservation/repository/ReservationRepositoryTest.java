package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {
    private Member member;
    private Theme theme;
    private Time time;
    private ReservationDetail reservationDetail;
    private Reservation reservation;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        member = new Member("범블비", "aa@email.com", "1111");
        theme = new Theme("Harry Potter", "해리포터와 도비", "thumbnail.jpg");
        time = new Time(LocalTime.of(12, 0));
        reservationDetail = new ReservationDetail(theme, time, LocalDate.MAX);
        reservation = new Reservation(member, reservationDetail);

        entityManager.persist(member);
        entityManager.persist(time);
        entityManager.persist(theme);
        entityManager.persist(reservationDetail);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("성공 : 예약 정보를 DB에 저장할 수 있다.")
    void save() {
        // when
        reservationRepository.save(reservation);

        // then
        List<Reservation> expected = reservationRepository.findAll();
        assertThat(reservation).isEqualTo(expected.get(0));
    }

    @Test
    @DisplayName("성공 : DB에 있는 모든 예약을 모두 얻을 수 있다.")
    void findAllByOrderByDateAsc() {
        // Given
        List<Reservation> expected = reservationRepository.findAll();
        entityManager.persist(reservation);

        // When
        List<Reservation> reservations = reservationRepository.findAllByOrderByDetailDateAsc();

        // Then
        assertThat(reservations).containsExactly(reservation);
    }

    @Test
    @DisplayName("성공 : 해당 테마에 해당 날짜인 예약을 모두 얻을 수 있다.")
    void findAllByDetailTheme_IdAndDetailDate() {
        // Given
        entityManager.persist(reservation);

        // When
        List<Reservation> reservations
                = reservationRepository.findAllByDetailTheme_IdAndDetailDate(theme.getId(), LocalDate.MIN);

        // Then
        assertThat(reservations).hasSize(0);
    }

    @Test
    @DisplayName("성공 : 해당 멤버가 예약한 예약을 모두 얻을 수 있다.")
    void findAllByMember_Id() {
        // Given
        entityManager.persist(reservation);

        // When
        List<Reservation> reservationIds = reservationRepository.findAllByMember_Id(member.getId());

        // Then
        assertThat(reservationIds).hasSize(1);
    }

    @Test
    @DisplayName("성공 : 해당 멤버가 예약한 예약을 모두 날짜순으로 얻을 수 있다.")
    void findAllByMember_IdOrderByDetailDateAsc() {
        // Given
        entityManager.persist(reservation);

        // When
        List<Reservation> reservations
                = reservationRepository.findAllByMember_IdOrderByDetailDateAsc(member.getId());

        // Then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("성공 : 해당 멤버가 예약한 예약을 모두 날짜순으로 얻을 수 있다.")
    void findByDetail_IdAndMember_Id() {
        // Given
        entityManager.persist(reservation);

        // When
        Reservation actual = reservationRepository
                .findByDetail_IdAndMember_Id(reservationDetail.getId(), member.getId())
                .get();

        // Then
        assertThat(actual).isEqualTo(reservation);
    }

    @Test
    @DisplayName("성공 : 해당 멤버가 예약한 예약을 모두 날짜순으로 얻을 수 있다.")
    void findByDetail_Id() {
        // Given
        entityManager.persist(reservation);

        // When
        Reservation actual = reservationRepository
                .findByDetail_Id(reservationDetail.getId())
                .get();

        // Then
        assertThat(actual).isEqualTo(reservation);
    }

    @Test
    @DisplayName("성공 : id로 회원 정보를 찾아 지운다.")
    void deleteById() {
        // Given
        entityManager.persist(reservation);

        // When
        reservationRepository.deleteById(reservation.getId());

        // Then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }
}
