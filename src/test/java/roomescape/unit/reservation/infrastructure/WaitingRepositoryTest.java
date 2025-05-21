package roomescape.unit.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.infrastructure.WaitingRepository;

@DataJpaTest
public class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 대기를_저장한다() {
        // given
        Member member = Member.builder()
                .name("member1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).bulid();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("description")
                .thumbnail("thumbnail").build();
        TimeSlot timeSlot = TimeSlot.builder().startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(member);
        Waiting waiting = Waiting.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .theme(theme)
                .timeSlot(timeSlot)
                .build();
        // when
        Waiting savedWaiting = waitingRepository.save(waiting);
        // then
        Waiting findWaiting = entityManager.find(Waiting.class, savedWaiting.getId());
        assertThat(findWaiting).isNotNull();
        assertThat(findWaiting.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }
}
