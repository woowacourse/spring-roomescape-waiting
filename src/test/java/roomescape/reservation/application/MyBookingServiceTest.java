package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
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
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MyBookingServiceTest {

    @Autowired
    MyBookingService myBookingService;

    @PersistenceContext
    EntityManager em;

    private ReservationTime time;
    private ReservationTheme theme;
    private Member member;
    private LocalDate tomorrow = LocalDate.now();

    @BeforeEach
    void setUp() {
        time = ReservationTestFixture.getReservationTimeFixture();
        em.persist(time);

        theme = ReservationTestFixture.getReservationThemeFixture();
        em.persist(theme);

        member = ReservationTestFixture.getUserFixture();
        em.persist(member);
    }

    @DisplayName("삭제시 해당하는 id가 없다면 예외를 던진다")
    @Test
    void deleteNotExists() {
        assertThatThrownBy(() -> myBookingService.deleteById(1L)).isInstanceOf(
            ResourceNotFoundException.class);
    }

    @DisplayName("삭제가 잘 동작한다")
    @Test
    void deleteWhenExists() {
        Waiting waiting = new Waiting(theme, tomorrow, time, member);
        em.persist(waiting);
        Long waitingId = waiting.getId();

        assertDoesNotThrow(() -> myBookingService.deleteById(waitingId));
        assertThat(em.find(Waiting.class, waitingId)).isNull();
    }
}
