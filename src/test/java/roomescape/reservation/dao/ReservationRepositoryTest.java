package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationContent;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {
    private static Member MEMBER;
    private static Time TIME;
    private static Theme THEME;
    private static ReservationContent RESERVATION_CONTENT;
    private static Reservation RESERVATION;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        MEMBER = entityManager.merge(new Member(1L, "범블비", "aa@email.com", "1111"));
        TIME = entityManager.merge(new Time(1L, LocalTime.of(12, 0)));
        THEME = entityManager.merge(new Theme(1L, "Harry Potter", "해리포터와 도비", "thumbnail.jpg"));
        RESERVATION_CONTENT = entityManager.merge(new ReservationContent(LocalDate.MAX, TIME, THEME));
        RESERVATION = new Reservation(MEMBER, RESERVATION_CONTENT);
    }

    @Test
    @DisplayName("데이터를 정상적으로 저장한다.")
    void saveReservation() {
        reservationRepository.save(RESERVATION);

        List<Reservation> expected = reservationRepository.findAll();
        assertThat(RESERVATION).isEqualTo(expected.iterator()
                .next());
    }

    @Test
    @DisplayName("데이터를 날짜순으로 정상적으로 조회한다.")
    void findAllByOrderByReservationContent_Date() {
        // Given
        entityManager.merge(RESERVATION);

        // When
        List<Reservation> reservations = reservationRepository.findAllByOrderByReservationContent_Date();

        // Then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("해당 테마에 해당 날짜인 예약을 모두 정상적으로 조회한다.")
    void findAllByReservationContent_Theme_IdAndReservationContent_Date() {
        // Given
        entityManager.merge(RESERVATION);

        // When
        List<Reservation> reservations = reservationRepository.findAllByReservationContent_Theme_IdAndReservationContent_Date(
                THEME.getId(), LocalDate.MAX);

        // Then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("해당 멤버가 예약한 예약의 id를 정상적으로 조회한다.")
    void findAllByMember_Id() {
        // Given
        entityManager.merge(RESERVATION);

        // When
        List<Reservation> reservationIds = reservationRepository.findAllByMember_Id(MEMBER.getId());

        // Then
        assertThat(reservationIds).hasSize(1);
    }

    @Test
    @DisplayName("데이터를 정상적으로 삭제한다.")
    void deleteReservations() {
        Reservation expectedReservation = entityManager.merge(RESERVATION);

        reservationRepository.deleteById(expectedReservation.getId());

        assertThat(reservationRepository.findById(expectedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하는 예약시간인지 확인한다.")
    void countReservationTime() {
        Reservation expectedReservation = entityManager.merge(RESERVATION);

        assertThat(reservationRepository.countReservationsByReservationContent_Time_Id(
                expectedReservation.getTimeId())).isEqualTo(1);
    }

}
