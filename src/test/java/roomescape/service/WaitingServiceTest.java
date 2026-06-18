package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.FakeWaitingRepository;

class WaitingServiceTest {

    private WaitingService waitingService;
    private FakeWaitingRepository waitingRepository;
    private Session session;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingService = new WaitingService(waitingRepository);

        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "test.com");
        session = new Session(1L, LocalDate.now().plusDays(1), timeSlot, theme);
    }

    @Test
    @DisplayName("대기를 저장하고 반환한다.")
    void save() {
        Waiting waiting = waitingService.save(Waiting.transientOf("브라운", session));
        assertThat(waiting.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("존재하는 대기를 식별자로 조회한다.")
    void findByIdOrThrow() {
        Waiting saved = waitingService.save(Waiting.transientOf("브라운", session));
        Waiting found = waitingService.findByIdOrThrow(saved.getId());
        assertThat(found.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("존재하지 않는 대기를 조회하면 예외가 발생한다.")
    void findByIdOrThrow_NotFound() {
        assertThatThrownBy(() -> waitingService.findByIdOrThrow(999L))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    @DisplayName("중복된 대기를 저장하려 하면 예외가 발생한다.")
    void validateNotDuplicate() {
        Waiting waiting = Waiting.transientOf("브라운", session);
        waitingService.save(waiting);
        assertThatThrownBy(() -> waitingService.validateNotDuplicate(Waiting.transientOf("브라운", session)))
                .isInstanceOf(DuplicateWaitingException.class);
    }

    @Test
    @DisplayName("세션으로 대기 목록을 조회한다.")
    void findBySession() {
        waitingService.save(Waiting.transientOf("브라운", session));
        assertThat(waitingService.findBySession(session)).hasSize(1);
    }
}
