package roomescape.waiting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Rank;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingInfoDataResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class WaitingRepositoryTest {

    private final Member member = new Member("test", "test@test.com", "12341234", Role.MEMBER);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("유저가 대기 중인 예약 정보를 가져올 수 있다.")
    @Test
    void findAllWaitingInfo() {
        // given
        Member otherMember = new Member("other", "test@test.com", "12341234", Role.MEMBER);

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("theme", "description", "thumbnail");

        Waiting otherWaiting = new Waiting(LocalDate.now().plusDays(1), time, theme, otherMember, LocalDateTime.now().minusMinutes(1));
        Waiting waiting = new Waiting(LocalDate.now().plusDays(1), time, theme, member, LocalDateTime.now());

        entityManager.persist(otherMember);
        entityManager.persist(member);
        entityManager.persist(time);
        entityManager.persist(theme);
        entityManager.persist(otherWaiting);
        entityManager.persist(waiting);

        // when
        List<WaitingInfoDataResponse> responses = waitingRepository.findAllWaitingInfoByMemberId(member.getId());

        // then
        assertSoftly((softly) -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.getFirst().rank()).isEqualTo(new Rank(2));
        });
    }
}
