package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;

import java.time.LocalTime;
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

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private WaitingRepository waitingRepository;

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
            boolean isDuplicated = waitingRepository.existsDuplicated(theme.getId(), time.getId(), member.getId());

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
            boolean isDuplicated = waitingRepository.existsDuplicated(theme.getId(), time.getId(), member.getId());

            // then
            assertThat(isDuplicated).isTrue();

        }
    }
}
