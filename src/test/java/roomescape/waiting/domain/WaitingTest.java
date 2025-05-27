package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationSpecFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class WaitingTest {

    @PersistenceContext
    private EntityManager entityManager;

    @DisplayName("Waiting 엔티티가 저장될 때 onCreate 메서드가 호출되어 createdAt 필드가 설정된다")
    @Test
    void onCreate() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting = new Waiting(member, spec);

        // when
        LocalDateTime beforePersist = LocalDateTime.now();
        entityManager.persist(waiting);
        entityManager.flush();

        // then
        assertThat(waiting.getCreatedAt()).isNotNull();
        assertThat(waiting.getCreatedAt()).isAfterOrEqualTo(beforePersist);
        assertThat(waiting.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
