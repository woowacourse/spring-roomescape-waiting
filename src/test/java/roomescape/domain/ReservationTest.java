package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidInputException;
import roomescape.domain.exception.PastReservationException;

class ReservationTest {

    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime createdAt = LocalDateTime.of(2023, 8, 1, 0, 0);

    public ReservationTest() {
        this.time = new ReservationTime(1L, LocalTime.of(15, 40));
        this.theme = new Theme(1L, "공포의 저택", "버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.",
                "https://picsum.photos/seed/haunted/400/250");
    }

    @Test
    void 예약_생성() {
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), createdAt, time, theme);

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2023, 8, 5));
        assertThat(reservation.getTime()).isEqualTo(time);
    }

    @Test
    void 이름이_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(1L, null, LocalDate.of(2023, 8, 5), createdAt, time, theme))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 이름이_공백이면_예외() {
        assertThatThrownBy(() -> new Reservation(1L, "   ", LocalDate.of(2023, 8, 5), createdAt, time, theme))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 날짜가_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", null, createdAt, time, theme))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 시간이_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), createdAt, null, theme))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 과거_날짜로_예약하면_예외() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 15, 0);
        LocalDate yesterday = now.toLocalDate().minusDays(1);

        assertThatThrownBy(() -> new Reservation("브라운", yesterday, now, time, theme))
                .isInstanceOf(PastReservationException.class)
                .hasMessage("과거 날짜로는 예약할 수 없습니다.");
    }

    @Test
    void 원래_createdAt_기준으로_과거여도_생성_가능() {
        LocalDateTime originalCreatedAt = LocalDateTime.of(2023, 7, 1, 0, 0);
        LocalDate pastDate = LocalDate.of(2023, 8, 5);

        assertThat(new Reservation(1L, "브라운", pastDate, originalCreatedAt, time, theme))
                .isNotNull();
    }

    @Test
    void withUpdated_과거_날짜면_예외() {
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), createdAt, time, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 15, 0);
        LocalDate pastDate = now.toLocalDate().minusDays(1);

        assertThatThrownBy(() -> reservation.withUpdated(pastDate, time, now))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    void validateCancellable_지난_예약이면_예외() {
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), createdAt, time, theme);
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 15, 0);

        assertThatThrownBy(() -> reservation.validateCancellable(now))
                .isInstanceOf(PastReservationException.class)
                .hasMessage("이미 지난 예약은 취소할 수 없습니다.");
    }

    @Test
    void validateCancellable_미래_예약은_취소_가능() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 15, 0);
        LocalDate futureDate = now.toLocalDate().plusDays(1);
        LocalDateTime futureCreatedAt = LocalDateTime.of(2026, 5, 1, 0, 0);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, futureCreatedAt, time, theme);

        reservation.validateCancellable(now);
    }

    @Test
    void id가_같으면_같은_예약() {
        Reservation a = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), createdAt, time, theme);
        Reservation b = new Reservation(1L, "다른이름", LocalDate.of(2023, 8, 5), createdAt, time, theme);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void id가_없으면_필드로_비교() {
        LocalDate date = LocalDate.of(2023, 8, 5);
        Reservation a = new Reservation(null, "브라운", date, createdAt, time, theme);
        Reservation b = new Reservation(null, "브라운", date, createdAt, time, theme);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void id가_없고_필드가_다르면_다른_예약() {
        Reservation a = new Reservation(null, "브라운", LocalDate.of(2023, 8, 5), createdAt, time, theme);
        Reservation b = new Reservation(null, "다른이름", LocalDate.of(2023, 8, 5), createdAt, time, theme);

        assertThat(a).isNotEqualTo(b);
    }
}
