package roomescape.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("test")
class JpaWaitingRepositoryTest {

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자가 예약 대기한 대기와 순번을 조회할 수 있다.")
    void findWaitingWithRankByMemberId() {
        //given
        Member member1 = TestFixture.createMemberByName("moru");
        Member member2 = TestFixture.createMemberByName("molru");
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme = TestFixture.createDefaultTheme();
        entityManager.persist(theme);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        entityManager.persist(time);

        Waiting waiting1 = TestFixture.createWaiting(member1, TestFixture.TEST_DATE, time, theme);
        Waiting waiting2 = TestFixture.createWaiting(member2, TestFixture.TEST_DATE, time, theme);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);

        //when
        List<WaitingWithRank> waitingWithRanks = jpaWaitingRepository.findWaitingWithRankByMemberId(member2.getId());

        //then
        assertAll(
                () -> assertThat(waitingWithRanks).hasSize(1),
                () -> assertThat(waitingWithRanks.getFirst().waiting()).isEqualTo(waiting2),
                () -> assertThat(waitingWithRanks.getFirst().rank()).isEqualTo(2)
        );
    }
}