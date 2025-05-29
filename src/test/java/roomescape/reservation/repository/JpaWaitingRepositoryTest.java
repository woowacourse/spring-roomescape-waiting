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
import roomescape.reservation.domain.ReservationDetails;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-data.sql")
class JpaWaitingRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    WaitingListCrudRepository waitingRepository;

    @Nested
    @DisplayName("예약대기 조회")
    class FindWaiting {

        @DisplayName("예약대기 목록을 조회할 수 있다")
        @Test
        void test1() {
            // when
            List<Waiting> waitings = waitingRepository.findAll();

            // then
            assertThat(waitings.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약대기 생성")
    class CreateWaiting {

        @DisplayName("새 예약대기를 저장할 수 있다")
        @Test
        void test1() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            ReservationDetails reservationDetails = new ReservationDetails(
                    LocalDate.now().plusDays(7),
                    new ReservationTime(1L, LocalTime.of(9, 0)),
                    new Theme(1L, "테마 A", "테마 A입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
            );
            Waiting waiting = new Waiting(null, member, reservationDetails);

            // when
            Waiting newWaiting = waitingRepository.save(waiting);

            // then
            assertThat(newWaiting.getId()).isEqualTo(2L);
        }

        @DisplayName("동일한 날짜, 시간, 테마, 멤버가 중복된 예약대기는 저장되지 않는다")
        @Test
        void test2() {
            // given
            Member member = LoginMemberFixture.getAdmin();
            ReservationDetails reservationDetails = new ReservationDetails(
                    LocalDate.now().plusDays(7),
                    new ReservationTime(1L, LocalTime.of(9, 0)),
                    new Theme(1L, "테마 A", "테마 A입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
            );
            Waiting waiting = new Waiting(null, member, reservationDetails);
            entityManager.persist(waiting);
            entityManager.flush();
            entityManager.clear();

            // when
            waitingRepository.save(waiting);
            List<Waiting> waitings = waitingRepository.findAll();

            // then
            assertThat(waitings.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("예약대기 삭제")
    class DeleteWaiting {

        @DisplayName("저장된 예약대기를 삭제할 수 있다")
        @Test
        void test1() {
            // when
            waitingRepository.deleteById(1L);
            Waiting expected = entityManager.find(Waiting.class, 1L);

            // then
            assertThat(expected).isNull();
        }
    }
}
