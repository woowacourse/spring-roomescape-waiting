package roomescape.reservationtime.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-data.sql")
class JpaReservationTimeRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ReservationTimeListCrudRepository timeRepository;

    @Nested
    @DisplayName("예약시간 조회")
    class FindReservationTime {

        @DisplayName("예약시간 목록을 조회할 수 있다")
        @Test
        void test1() {
            // when
            List<ReservationTime> times = timeRepository.findAll();

            // then
            assertThat(times.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약시간 생성")
    class CreateReservationTime {

        @DisplayName("새 예약시간을 저장할 수 있다")
        @Test
        void test1() {
            // given
            ReservationTime time = new ReservationTime(null, LocalTime.of(12, 0));

            // when
            ReservationTime newTime = timeRepository.save(time);

            // then
            assertThat(newTime.getId()).isEqualTo(2L);
        }

        @DisplayName("중복되는 시간은 저장되지 않는다")
        @Test
        void test2() {
            // given
            ReservationTime time = new ReservationTime(null, LocalTime.of(12, 0));
            entityManager.persist(time);
            entityManager.flush();
            entityManager.clear();

            // when
            timeRepository.save(time);
            List<ReservationTime> times = timeRepository.findAll();

            // then
            assertThat(times.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("예약시간 삭제")
    class DeleteReservationTime {

        @DisplayName("저장된 예약시간을 삭제할 수 있다")
        @Test
        void test1() {
            // when
            timeRepository.deleteById(1L);
            ReservationTime expected = entityManager.find(ReservationTime.class, 1L);

            // then
            assertThat(expected).isNull();
        }
    }
}
