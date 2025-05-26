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
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.entity.Waiting;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.entity.Theme;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("특정 시간에 대한 대기 예약이 존재하는지 확인할 수 있다.")
    void existsByDateAndTimeIdAndThemeIdAndMemberId() {
        // given
        var reservationTime = new ReservationTime(LocalTime.of(10, 0));
        var theme = new Theme("테마1", "설명1", "썸네일1");
        var member = new Member("미소", "miso@email.com", "miso", RoleType.USER);
        var date = LocalDate.now().plusDays(1);
        var waiting = new Waiting(date, reservationTime, theme, member);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(member);
        entityManager.persist(waiting);

        // when
        boolean exists = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                date,
                reservationTime.getId(),
                theme.getId(),
                member.getId()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 시간의 대기 예약 목록을 생성 시간 순으로 조회할 수 있다.")
    void findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt() {
        // given
        var reservationTime = new ReservationTime(LocalTime.of(10, 0));
        var theme = new Theme("테마1", "설명1", "썸네일1");
        var member1 = new Member("미소", "miso@email.com", "miso", RoleType.USER);
        var member2 = new Member("크루", "crew@email.com", "crew", RoleType.USER);
        var date = LocalDate.now().plusDays(1);
        var waiting1 = new Waiting(date, reservationTime, theme, member1);
        var waiting2 = new Waiting(date, reservationTime, theme, member2);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        // when
        List<Waiting> waitings = waitingRepository.findAllByDateAndTimeIdAndThemeIdOrderByCreatedAt(
                date,
                reservationTime.getId(),
                theme.getId()
        );

        // then
        assertAll(
                () -> assertThat(waitings.getFirst().getDate()).isEqualTo(date),
                () -> assertThat(waitings.getFirst().getTime()).isEqualTo(reservationTime),
                () -> assertThat(waitings.getFirst().getTheme()).isEqualTo(theme),
                () -> assertThat(waitings.getFirst().getMember()).isEqualTo(member1),
                () -> assertThat(waitings.get(1).getMember()).isEqualTo(member2)
        );
    }
} 
