package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("특정 테마와 날짜의 예약시간을 예약 여부와 함께 조회할 수 있다.")
    @Test
    void canFindReservationTimesWithBookState() {
        // given
        User member = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));
        Theme theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

        ReservationTime timeAt10 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime timeAt11 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        ReservationTime timeAt12 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(12, 0)));

        entityManager.persist(Reservation.createWithoutId(
                NEXT_DAY, ReservationStatus.BOOKED, timeAt10, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                NEXT_DAY, ReservationStatus.BOOKED, timeAt11, theme, member));

        entityManager.flush();

        // when
        List<ReservationTime> times =
                reservationTimeRepository.findReservationTimesWithBookState(theme.getId(), NEXT_DAY);

        // then
        assertThat(times)
                .extracting(ReservationTime::getId)
                .containsExactly(timeAt10.getId(), timeAt11.getId());
    }
}
