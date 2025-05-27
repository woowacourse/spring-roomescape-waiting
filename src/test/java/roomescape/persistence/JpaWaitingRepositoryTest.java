package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createMemberByName;
import static roomescape.TestFixture.createWaiting;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.persistence.dto.WaitingWithRankData;

@ActiveProfiles("test")
@DataJpaTest
class JpaWaitingRepositoryTest {

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("첫 번째 대기를 찾을 수 있다.")
    void findFirstWaiting() {
        // given
        Member member1 = createMemberByName("member1");
        Member member2 = createMemberByName("member2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme = createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = createDefaultReservationTime();
        entityManager.persist(time);

        Waiting waiting1 = createWaiting(member1, DEFAULT_DATE, time, theme);
        Waiting waiting2 = createWaiting(member2, DEFAULT_DATE, time, theme);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        // when
        Waiting result = jpaWaitingRepository.findFirstWaiting(DEFAULT_DATE, theme.getId(), time.getId())
                .orElseThrow();

        // then
        assertThat(result).isEqualTo(waiting1);
    }

    @Test
    @DisplayName("회원 ID로 대기를 찾을 수 있다.")
    void findByMemberId() {
        // given
        Member member = createDefaultMember();
        entityManager.persist(member);

        Theme theme = createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = createDefaultReservationTime();
        entityManager.persist(time);

        Waiting waiting = createWaiting(member, DEFAULT_DATE, time, theme);
        entityManager.persist(waiting);

        // when
        List<Waiting> result = jpaWaitingRepository.findByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.getFirst()).isEqualTo(waiting)
        );
    }

    @Test
    @DisplayName("회원 ID로 대기 순위와 함께 대기를 찾을 수 있다.")
    void findWaitingsWithRankByMemberId() {
        // given
        Member member1 = createMemberByName("member1");
        Member member2 = createMemberByName("member2");
        Theme theme = createDefaultTheme();
        ReservationTime time = createDefaultReservationTime();
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(theme);
        entityManager.persist(time);

        Waiting waiting1 = createWaiting(member1, DEFAULT_DATE, time, theme);
        Waiting waiting2 = createWaiting(member2, DEFAULT_DATE, time, theme);
        Waiting waiting3 = createWaiting(member1, DEFAULT_DATE, time, theme);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);
        entityManager.persist(waiting3);

        // when
        List<WaitingWithRankData> result = jpaWaitingRepository.findWaitingsWithRankByMemberId(member1.getId());

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result.get(0).rank()).isEqualTo(0),
                () -> assertThat(result.get(1).rank()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("모든 대기를 찾을 수 있다.")
    void findAllWaitings() {
        // given
        Member member1 = createMemberByName("member1");
        Member member2 = createMemberByName("member2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme = createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = createDefaultReservationTime();
        entityManager.persist(time);

        Waiting waiting1 = createWaiting(member1, DEFAULT_DATE, time, theme);
        Waiting waiting2 = createWaiting(member2, DEFAULT_DATE, time, theme);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        // when
        List<Waiting> result = jpaWaitingRepository.findAllWaitings();

        // then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(result).containsExactly(waiting1, waiting2)
        );
    }
}
