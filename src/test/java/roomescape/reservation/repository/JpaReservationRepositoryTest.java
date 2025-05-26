package roomescape.reservation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.LoginMemberFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetails;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-data.sql")
class JpaReservationRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ReservationListCrudRepository reservationRepository;

    @Nested
    @DisplayName("예약 조회")
    class FindReservation {

        @DisplayName("예약 목록을 조회할 수 있다")
        @Test
        void test1() {
            // when
            List<Reservation> reservations = reservationRepository.findAll();

            // then
            assertThat(reservations.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 생성")
    class CreateReservation {

        @DisplayName("새 예약을 저장할 수 있다")
        @Test
        void test1() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            ReservationDetails reservationDetails = new ReservationDetails(
                    LocalDate.now().plusDays(7),
                    new ReservationTime(1L, LocalTime.of(9, 0)),
                    new Theme(1L, "테마 A", "테마 A입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
            );
            Reservation reservation = new Reservation(null, member, reservationDetails);

            // when
            Reservation newReservation = reservationRepository.save(reservation);

            // then
            assertThat(newReservation.getId()).isEqualTo(2L);
        }

        @DisplayName("동일한 날짜, 시간, 테마가 중복된 예약은 저장되지 않는다")
        @Test
        void test2() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            ReservationDetails reservationDetails = new ReservationDetails(
                    LocalDate.now().plusDays(7),
                    new ReservationTime(1L, LocalTime.of(9, 0)),
                    new Theme(1L, "테마 A", "테마 A입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
            );
            Reservation reservation = new Reservation(null, member, reservationDetails);
            entityManager.persist(reservation);
            entityManager.flush();
            entityManager.clear();

            // when
            reservationRepository.save(reservation);
            List<Reservation> reservations = reservationRepository.findAll();

            // then
            assertThat(reservations.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("예약 삭제")
    class DeleteReservation {

        @DisplayName("저장된 예약을 삭제할 수 있다")
        @Test
        void test1() {
            // when
            reservationRepository.deleteById(1L);
            Reservation expected = entityManager.find(Reservation.class, 1L);

            // then
            assertThat(expected).isNull();
        }
    }
}
