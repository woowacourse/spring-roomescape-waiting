package roomescape.reservation.unit.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("필터링된 예약 목록을 조회할 수 있다.")
    void findAllFiltered() {
        // given
        var reservationTime = new ReservationTime(LocalTime.of(10, 0));
        var theme = new Theme("테마1", "설명1", "썸네일1");
        var member = new Member("미소", "miso@email.com", "miso", RoleType.USER);
        var date = LocalDate.now().plusDays(1);
        var reservation1 = new Reservation(date, reservationTime, theme, member);
        var reservation2 = new Reservation(LocalDate.now().minusDays(5), reservationTime, theme, member);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(member);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        // when
        List<Reservation> reservations = reservationRepository.findAllFiltered(
                1L,
                1L,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(2)
        );

        // then
        assertAll(
                () -> assertThat(reservations.size()).isEqualTo(1),
                () -> assertThat(reservations.getFirst().getDate()).isEqualTo(date),
                () -> assertThat(reservations.getFirst().getTime()).isEqualTo(reservationTime),
                () -> assertThat(reservations.getFirst().getTheme()).isEqualTo(theme),
                () -> assertThat(reservations.getFirst().getMember()).isEqualTo(member)
        );
    }
}
