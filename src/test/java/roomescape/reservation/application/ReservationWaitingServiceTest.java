package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.repository.WaitingRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReservationWaitingServiceTest {

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @Autowired
    WaitingRepository waitingRepository;

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

    @DisplayName("예약 대기 생성 성공")
    @Test
    void createWaitingSuccess() {
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(
            member1.getId(), tomorrow, time.getId(), theme.getId()
        );
        WaitingServiceResponse response = reservationWaitingService.create(request);
        assertThat(response.name()).isEqualTo(member1.getName());
        assertThat(response.date()).isEqualTo(tomorrow);
        assertThat(response.startAt()).isEqualTo(time.getStartAt());
        assertThat(response.themeName()).isEqualTo(theme.getName());
        assertThat(waitingRepository.findAll()).hasSize(1);
    }

    @DisplayName("중복 예약 대기 생성시 예외 발생")
    @Test
    void createWaitingDuplicateException() {
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(
            member1.getId(), tomorrow, time.getId(), theme.getId()
        );
        reservationWaitingService.create(request);
        assertThatThrownBy(() -> reservationWaitingService.create(request))
            .isInstanceOf(InvalidReservationTimeException.class);
    }

    @DisplayName("예약 대기 삭제 성공")
    @Test
    void deleteWaitingExists() {
        Waiting waiting = new Waiting(theme, tomorrow, time, member2);
        waitingRepository.save(waiting);

        assertThat(waitingRepository.findAll()).hasSize(1);
        reservationWaitingService.deleteById(waiting.getId());
        assertThat(waitingRepository.findAll()).isEmpty();
    }

    @DisplayName("존재하지 않는 예약 대기 삭제시 예외 발생")
    @Test
    void deleteWaitingNotExists() {
        assertThatThrownBy(() -> reservationWaitingService.deleteById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
