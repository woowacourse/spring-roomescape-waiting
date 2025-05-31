package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.ReservationTestFixture;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.member.model.Member;
import roomescape.reservation.application.dto.response.MyWaitingServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AdminWaitingServiceTest {

    @Autowired
    AdminWaitingService adminWaitingService;

    @PersistenceContext
    EntityManager em;

    private ReservationTime time;
    private ReservationTheme theme;
    private Member member1;
    private Member member2;
    private LocalDate tomorrow = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        time = ReservationTestFixture.getReservationTimeFixture();
        em.persist(time);

        theme = ReservationTestFixture.getReservationThemeFixture();
        em.persist(theme);

        member1 = ReservationTestFixture.createUser("두리", "d@naver.com", "1234");
        em.persist(member1);

        member2 = ReservationTestFixture.createUser("웨이드", "w@naver.com", "1234");
        em.persist(member2);

        em.flush();
    }

    @Test
    @DisplayName("예약 대기 전체 조회")
    void getAllWaitings_basic() {
        Waiting waiting = new Waiting(theme, tomorrow, time, member1);
        em.persist(waiting);

        List<MyWaitingServiceResponse> expected = adminWaitingService.getAllWaitings();
        SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(expected).hasSize(1);
                softly.assertThat(expected.get(0).waitingId()).isEqualTo(waiting.getId());
                softly.assertThat(expected.get(0).memberName()).isEqualTo(member1.getName());
            }
        );
    }

    @DisplayName("삭제시 해당하는 id가 없다면 예외를 던진다")
    @Test
    void deleteNotExists() {
        assertThatThrownBy(() -> adminWaitingService.deleteById(1L)).isInstanceOf(
            ResourceNotFoundException.class);
    }

    @DisplayName("삭제가 잘 동작한다")
    @Test
    void deleteWhenExists() {
        Waiting waiting = new Waiting(theme, tomorrow, time, member1);
        em.persist(waiting);
        Long waitingId = waiting.getId();

        assertDoesNotThrow(() -> adminWaitingService.deleteById(waitingId));
        assertThat(em.find(Waiting.class, waitingId)).isNull();
    }
}
