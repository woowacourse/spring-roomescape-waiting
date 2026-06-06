package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidOwnershipException;

class WaitingTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 대기 객체가 생성된다.")
    void createValidWaiting() {
        Session session = createMockSlot();
        Waiting waiting = new Waiting(1L, "브라운", session, 1);
        assertThat(waiting.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("대기자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void createInvalidNameThrowsException() {
        Session session = createMockSlot();
        assertThatThrownBy(() -> new Waiting(1L, "", session, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transientOf를 통해 비영속 상태의 대기 객체를 생성할 수 있다.")
    void transientOfCreatesTransientWaiting() {
        Session session = createMockSlot();
        Waiting waiting = Waiting.transientOf("브라운", session);
        assertThat(waiting.getId()).isNull();
    }

    @Test
    @DisplayName("대기 소유자가 일치하지 않으면 예외가 발생한다.")
    void validateModifiableThrowsException() {
        Waiting waiting = new Waiting(1L, "브라운", createMockSlot(), 1);
        assertThatThrownBy(() -> waiting.validateModifiable("포비", LocalDateTime.now()))
                .isInstanceOf(InvalidOwnershipException.class);
    }

    private Session createMockSlot() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "설명", "url");
        return new Session(1L, LocalDate.now(), timeSlot, theme);
    }
}
