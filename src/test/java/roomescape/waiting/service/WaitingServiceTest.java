package roomescape.waiting.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@Import(WaitingService.class)
class WaitingServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingService waitingService;

    @DisplayName("자신이 생성한 대기 예약만 삭제할 수 있다.")
    @Test
    void deleteOnlyMyWaiting() {
        // given
        Member other = new Member("other", "test@test.com", "12341234", Role.MEMBER);
        Member me = new Member("me", "test@test.com", "12341234", Role.MEMBER);
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("title", "desc", "thumbnail");
        Waiting waiting = new Waiting(LocalDate.now().plusDays(1), time, theme, me, LocalDateTime.now());

        entityManager.persist(other);
        entityManager.persist(me);
        entityManager.persist(time);
        entityManager.persist(theme);
        entityManager.persist(waiting);

        // when & then
        assertThatThrownBy(() -> {
            waitingService.delete(waiting.getId(), LoginMember.of(other));
        }).isInstanceOf(EntityNotFoundException.class);
    }
}
