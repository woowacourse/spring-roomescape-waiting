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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("날짜와 테마 ID로 모든 예약되어 있는 시간을 조회할 수 있다.")
    void findAllReservedTimeByDateAndThemeId() {
        // given
        var reservationTime1 = new ReservationTime(LocalTime.of(10, 0));
        var reservationTime2 = new ReservationTime(LocalTime.of(11, 0));
        var theme1 = new Theme("테마1", "설명1", "썸네일1");
        var theme2 = new Theme("테마2", "설명2", "썸네일2");
        var member1 = new Member("미소", "miso@email.com", "miso", RoleType.USER);
        var member2 = new Member("훌라", "hula@email.com", "hula", RoleType.USER);
        var date = LocalDate.now().plusDays(1);
        var reservation1 = new Reservation(date, reservationTime1, theme1, member1);
        var reservation2 = new Reservation(date, reservationTime2, theme2, member2);
        var reservation3 = new Reservation(date, reservationTime2, theme1, member2);
        entityManager.persist(reservationTime1);
        entityManager.persist(reservationTime2);
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllReservedTimeByDateAndThemeId(
                date,
                1L
        );

        // then
        assertAll(
                () -> assertThat(reservationTimes.size()).isEqualTo(2),
                () -> assertThat(reservationTimes.get(0).getStartAt()).isEqualTo(reservationTime1.getStartAt()),
                () -> assertThat(reservationTimes.get(1).getStartAt()).isEqualTo(reservationTime2.getStartAt())
        );
    }
}
