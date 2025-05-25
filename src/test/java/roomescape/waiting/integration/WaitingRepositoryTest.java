package roomescape.waiting.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@DataJpaTest
public class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("id값을 기준으로 더 더 작은 갯수를 구한다.")
    @Test
    void coundByReservationSlotAndIdLessThan() {
        // given
        var theme = new Theme("테마", "설명", "썸네일");
        var time = new ReservationTime(LocalTime.of(10, 0));
        var date = LocalDate.now().plusDays(1);
        var slot = new ReservationSlot(date, time, theme);

        var member_1 = new Member("미소", "miso@email.com", "password", RoleType.USER);
        var member_2 = new Member("훌라", "hula@email.com", "password", RoleType.USER);
        var member_3 = new Member("앤지", "anzy@email.com", "password", RoleType.USER);

        var waiting_1 = new Waiting(slot, member_1);
        var waiting_2 = new Waiting(slot, member_2);
        var waiting_3 = new Waiting(slot, member_3);

        entityManager.persist(theme);
        entityManager.persist(time);
        entityManager.persist(slot);

        entityManager.persist(member_1);
        entityManager.persist(member_2);
        entityManager.persist(member_3);

        entityManager.persist(waiting_1);
        entityManager.persist(waiting_2);
        entityManager.persist(waiting_3);

        // when
        var count = waitingRepository.countByReservationSlotAndIdLessThan(slot, waiting_3.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }
}
