package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 세션 객체가 생성된다.")
    void createValidSession() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "설명", "url");
        Session session = new Session(1L, LocalDate.now(), timeSlot, theme);
        assertThat(session.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("필수 정보(날짜, 시간, 테마) 중 하나라도 null이면 예외가 발생한다.")
    void createInvalidSessionThrowsException() {
        Theme theme = new Theme(1L, "공포", "설명", "url");
        assertThatThrownBy(() -> new Session(1L, null, null, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transientOf를 통해 비영속 상태의 세션 객체를 생성할 수 있다.")
    void transientOfCreatesTransientSession() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "설명", "url");
        Session session = Session.transientOf(LocalDate.now(), timeSlot, theme);
        assertThat(session.getId()).isNull();
    }
}
