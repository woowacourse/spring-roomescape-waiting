package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class WaitingTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 1, 0, 0);

    @Test
    void 유효한_값으로_예약대기을_생성하면_필드가_저장된다() {
        Waiting waiting = new Waiting(1L, "브라운", LocalDate.of(2026, 5, 1), TIME, THEME);

        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(waiting.getTime()).isEqualTo(TIME);
    }

    @Test
    void 예약대기자_이름이_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Waiting(1L, "", LocalDate.of(2026, 5, 1), TIME, THEME))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약대기자_이름이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Waiting(1L, null, LocalDate.of(2026, 5, 1), TIME, THEME))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약대기_날짜가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Waiting(1L, "브라운", null, TIME, THEME))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약대기_시간이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Waiting(1L, "브라운", LocalDate.of(2026, 5, 1), null, THEME))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약대기_테마가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Waiting(1L, "브라운", LocalDate.of(2026, 5, 1), TIME, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 미래_시간으로_예약대기를_생성할_수_있다() {
        assertThatNoException().isThrownBy(
                () -> Waiting.create("브라운", LocalDate.of(2026, 5, 10), TIME, THEME, NOW));
    }

    @Test
    void 과거_시간으로_예약대기를_생성하면_도메인_충돌_예외가_발생한다() {
        assertThatThrownBy(
                () -> Waiting.create("브라운", LocalDate.of(2026, 4, 1), TIME, THEME, NOW))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 본인의_예약_대기가_아니면_취소할_수_없다() {
        Waiting waiting = new Waiting(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME);

        assertThatThrownBy(() -> waiting.validateOwner("어셔"))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 승격한_대기가_예약과_같은_정보를_가진다() {
        //given
        Waiting waiting = new Waiting(
                1L,
                "밍구",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        //when
        Reservation reservation = waiting.promote(NOW);

        //then
        assertThat(reservation.getName()).isEqualTo(waiting.getName());
        assertThat(reservation.getDate()).isEqualTo(waiting.getDate());
        assertThat(reservation.getTime()).isEqualTo(waiting.getTime());
        assertThat(reservation.getTheme()).isEqualTo(waiting.getTheme());
    }

    @Test
    void 지난_시간의_대기는_승격할_수_없다() {
        //given
        Waiting waiting = new Waiting(
                1L,
                "밍구",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        //when & then
        assertThatThrownBy(() -> waiting.promote(LocalDate.of(2026, 5, 11).atStartOfDay()))
                .isInstanceOf(DomainConflictException.class);
    }
}
