package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingWithRank;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("회원의 예약 대기 목록을 순번과 함께 구할 수 있다.")
    @Test
    void findWithRankingByMember() {
        // given
        Member targetMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원1", "member1@test.com", "qwer1234!"));
        Member otherMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원2", "member2@test.com", "qwer1234!"));

        ReservationTime time = entityManager.persist(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

        Waiting waitingWithOtherMember = entityManager.persist(
                Waiting.createWithoutId(NEXT_DAY, theme, time, otherMember));
        Waiting waitingWithTargetMember = entityManager.persist(
                Waiting.createWithoutId(NEXT_DAY, theme, time, targetMember));

        entityManager.flush();

        // when
        List<WaitingWithRank> waitingWithRankings = waitingRepository.findWithRankingByMember(targetMember.getId());

        // then
        assertAll(
                () -> assertThat(waitingWithRankings).hasSize(1),
                () -> assertThat(waitingWithRankings.getFirst().id()).isEqualTo(waitingWithTargetMember.getId()),
                () -> assertThat(waitingWithRankings.getFirst().rank()).isEqualTo(2)
        );
    }

    @Nested
    @DisplayName("중복된 대기 데이터 존재여부를 확인할 수 있다.")
    public class existsDuplicated {

        @DisplayName("중복이 아닌 경우 false를 리턴한다.")
        @Test
        void isNotDuplicate() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));

            entityManager.flush();

            // when
            boolean isDuplicated = waitingRepository.existsDuplicated(
                    theme.getId(), NEXT_DAY, time.getId(), member.getId());

            // then
            assertThat(isDuplicated).isFalse();
        }

        @DisplayName("중복인 경우 true를 리턴한다.")
        @Test
        void isDuplicate() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Waiting waiting = entityManager.persist(Waiting.createWithoutId(NEXT_DAY, theme, time, member));

            entityManager.flush();

            // when
            boolean isDuplicated = waitingRepository.existsDuplicated(
                    theme.getId(), NEXT_DAY, time.getId(), member.getId());

            // then
            assertThat(isDuplicated).isTrue();

        }
    }

    @DisplayName("특정 테마의 날짜시간에 대한 첫번째 예약 대기를 조회할 수 있다.")
    @Test
    void findFirstWaiting() {
        // given
        ReservationTime time = entityManager.persist(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
        Member firstMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member1@test.com", "qwer1234!"));
        Member secondMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member2@test.com", "qwer1234!"));
        Waiting firstWaiting = entityManager.persist(Waiting.createWithoutId(NEXT_DAY, theme, time, firstMember));
        Waiting secondWaiting = entityManager.persist(Waiting.createWithoutId(NEXT_DAY, theme, time, secondMember));

        // when
        Optional<Waiting> findWaiting = waitingRepository.findFirstWaiting(theme.getId(), NEXT_DAY, time.getId());

        // then
        assertAll(
                () -> assertThat(findWaiting).isPresent(),
                () -> assertThat(findWaiting.get()).isEqualTo(firstWaiting)
        );
    }
}
