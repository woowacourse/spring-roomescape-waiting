package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Role;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.entity.Waiting;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("특정 게임 스케줄과 회원에 대한 예약대기가 이미 존재하는지 확인한다")
    void existsByGameScheduleIdAndMemberId() {
        // given
        Member member = Member.withoutId("어드민", "admin@email.com", "password", Role.ADMIN);
        Theme theme = Theme.withoutId("테마1", "테마 1입니다.", "썸네일1");
        ReservationTime time = ReservationTime.withoutId(LocalTime.of(10, 0));

        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(time);

        GameSchedule gameSchedule = GameSchedule.withoutId(LocalDate.now().plusDays(1), time, theme);
        entityManager.persist(gameSchedule);

        Waiting waiting = Waiting.withoutId(member, gameSchedule);
        entityManager.persist(waiting);
        entityManager.flush();

        // when
        boolean exists = waitingRepository.existsByGameScheduleIdAndMemberId(gameSchedule.getId(), member.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게임 스케줄과 회원에 대한 예약대기 조회 시 false를 반환한다")
    void existsByGameScheduleIdAndMemberId_NotExists() {
        // given
        Member member = Member.withoutId("어드민", "admin@email.com", "password", Role.ADMIN);
        Theme theme = Theme.withoutId("테마1", "테마 1입니다.", "썸네일1");
        ReservationTime time = ReservationTime.withoutId(LocalTime.of(10, 0));

        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(time);

        GameSchedule gameSchedule = GameSchedule.withoutId(LocalDate.now().plusDays(1), time, theme);
        entityManager.persist(gameSchedule);
        entityManager.flush();

        // when
        boolean exists = waitingRepository.existsByGameScheduleIdAndMemberId(gameSchedule.getId(), member.getId());

        // then
        assertThat(exists).isFalse();
    }
} 
