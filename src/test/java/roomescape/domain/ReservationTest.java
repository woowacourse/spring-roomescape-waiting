package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.exception.auth.WrongStoreAccessException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservationTest {

    private static final ReservationTime SAMPLE_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final LocalDate SAMPLE_DATE = LocalDate.now().plusDays(1);

    @Test
    void memberId가_null이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                null,
                SAMPLE_DATE,
                SAMPLE_TIME,
                1L,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void memberId가_음수이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                -1L,
                SAMPLE_DATE,
                SAMPLE_TIME,
                1L,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 양수여야 합니다.");
    }

    @Test
    void 날짜가_null이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                null,
                SAMPLE_TIME,
                1L,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 비어 있을 수 없습니다.");
    }

    @Test
    void 예약시간이_null이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                SAMPLE_DATE,
                null,
                1L,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약시간은 비어 있을 수 없습니다.");
    }

    @Test
    void themeId가_null이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                SAMPLE_DATE,
                SAMPLE_TIME,
                null,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void themeId가_음수이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                SAMPLE_DATE,
                SAMPLE_TIME,
                -1L,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 ID는 양수여야 합니다.");
    }

    @Test
    void storeId가_null이면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                SAMPLE_DATE,
                SAMPLE_TIME,
                1L,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void storeId가_0이하면_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L,
                1L,
                SAMPLE_DATE,
                SAMPLE_TIME,
                1L,
                0L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 ID는 양수여야 합니다.");
    }

    @Test
    void 같은_매장_매니저는_예약을_수정할_수_있다() {
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member sameStoreManager = new Member(
                4L, "manager-gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

        assertThatCode(() -> reservation.validateStoreOwnership(sameStoreManager))
                .doesNotThrowAnyException();
    }

    @Test
    void 다른_매장_매니저는_예약을_수정할_수_없다() {
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member otherStoreManager = new Member(
                5L, "manager-hongdae@email.com", "password", "홍대매니저", Role.MANAGER, 2L);

        assertThatThrownBy(() -> reservation.validateStoreOwnership(otherStoreManager))
                .isInstanceOf(WrongStoreAccessException.class);
    }

    @Test
    void storeId가_없는_일반_사용자는_예약을_수정할_수_없다() {
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member regularUser = new Member(
                2L, "brown@email.com", "password", "브라운", Role.USER, null);

        assertThatThrownBy(() -> reservation.validateStoreOwnership(regularUser))
                .isInstanceOf(WrongStoreAccessException.class);
    }
}
