package roomescape.waiting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Rank;
import roomescape.waiting.domain.ReservationInformation;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingInfoDataResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class WaitingRepositoryTest {

    private final Member member = new Member("test", "test@test.com", "12341234", Role.MEMBER);
    private final Member otherMember = new Member("other", "test@test.com", "12341234", Role.MEMBER);

    private final ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
    private final Theme theme = new Theme("theme", "description", "thumbnail");


    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setup() {
        entityManager.persist(otherMember);
        entityManager.persist(member);
        entityManager.persist(time);
        entityManager.persist(theme);
    }

    @DisplayName("유저가 대기 중인 예약 정보를 가져올 수 있다.")
    @Test
    void findAllWaitingInfo() {
        // given
        Waiting otherWaiting = new Waiting(LocalDate.now().plusDays(1), time, theme, otherMember, LocalDateTime.now().minusMinutes(1));
        Waiting waiting = new Waiting(LocalDate.now().plusDays(1), time, theme, member, LocalDateTime.now());

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

    @DisplayName("예약 정보에 해당하는 첫 번째 대기 예약을 조회할 수 있다. 2")
    @Test
    void findFirstByReservationInformation2() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime firstCreated = LocalDateTime.now();
        LocalDateTime secondCreated = firstCreated.plusMinutes(1);

        Waiting firstWaiting = new Waiting(date, time, theme, member, firstCreated);
        Waiting secondWaiting = new Waiting(date, time, theme, otherMember, secondCreated);

        entityManager.persist(firstWaiting);
        entityManager.persist(secondWaiting);

        // when
        Waiting result = waitingRepository.findFirstByReservationInformation(new ReservationInformation(date, time, theme));

        // then
        assertThat(result.getId()).isEqualTo(firstWaiting.getId());
    }
}
