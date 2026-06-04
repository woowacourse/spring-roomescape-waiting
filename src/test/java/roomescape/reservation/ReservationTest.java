package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.auth.exception.WrongStoreAccessException;
import roomescape.member.Member;
import roomescape.reservation.exception.PastReservationCancelNotAllowedException;
import roomescape.reservation.exception.PastReservationNotAllowedException;
import roomescape.reservation.exception.ReservationOwnerMismatchException;
import roomescape.reservationtime.ReservationTime;

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

    @Test
    void 본인이_예약한_경우_isReservedBy는_true를_반환한다() {
        long memberId = 1L;
        Reservation reservation = new Reservation(1L, memberId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);

        assertThat(reservation.isReservedBy(memberId)).isTrue();
    }

    @Test
    void 다른_사람이_예약한_경우_isReservedBy는_false를_반환한다() {
        long ownerId = 1L;
        long otherId = 2L;
        Reservation reservation = new Reservation(1L, ownerId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);

        assertThat(reservation.isReservedBy(otherId)).isFalse();
    }

    @Test
    void 본인이_미래_슬롯으로_변경하면_예외가_발생하지_않는다() {
        // given: 본인 예약 + 미래 변경 날짜
        long memberId = 1L;
        Reservation reservation = new Reservation(1L, memberId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        LocalDate futureDate = LocalDate.now().plusDays(2);

        // when & then: 변경 성공
        assertThatCode(() -> reservation.changeTo(memberId, futureDate, SAMPLE_TIME))
                .doesNotThrowAnyException();
    }

    @Test
    void 본인이_아닌_사용자가_변경하면_ReservationOwnerMismatchException() {
        // given: 다른 사용자가 본인 예약을 변경 시도
        long ownerId = 1L;
        long otherId = 2L;
        Reservation reservation = new Reservation(1L, ownerId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        LocalDate futureDate = LocalDate.now().plusDays(2);

        // when & then: 소유권 미스매치 예외
        assertThatThrownBy(() -> reservation.changeTo(otherId, futureDate, SAMPLE_TIME))
                .isInstanceOf(ReservationOwnerMismatchException.class);
    }

    @Test
    void 본인이_과거_슬롯으로_변경하면_PastReservationNotAllowedException() {
        // given: 본인 예약 + 과거 변경 날짜
        long memberId = 1L;
        Reservation reservation = new Reservation(1L, memberId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // when & then: 과거 시점 변경 불가
        assertThatThrownBy(() -> reservation.changeTo(memberId, pastDate, SAMPLE_TIME))
                .isInstanceOf(PastReservationNotAllowedException.class);
    }

    @Test
    void 같은_매장_매니저가_미래_슬롯으로_변경하면_예외가_발생하지_않는다() {
        // given: 같은 매장 매니저 + 미래 슬롯
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member sameStoreManager = new Member(
                4L, "manager-gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);
        LocalDate futureDate = LocalDate.now().plusDays(2);

        // when & then: 변경 성공
        assertThatCode(() -> reservation.changeToByManager(sameStoreManager, futureDate, SAMPLE_TIME))
                .doesNotThrowAnyException();
    }

    @Test
    void 다른_매장_매니저가_변경하면_WrongStoreAccessException() {
        // given: 다른 매장 매니저
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member otherStoreManager = new Member(
                5L, "manager-hongdae@email.com", "password", "홍대매니저", Role.MANAGER, 2L);
        LocalDate futureDate = LocalDate.now().plusDays(2);

        // when & then: 매장 접근 권한 위반 예외
        assertThatThrownBy(() -> reservation.changeToByManager(otherStoreManager, futureDate, SAMPLE_TIME))
                .isInstanceOf(WrongStoreAccessException.class);
    }

    @Test
    void 매니저가_과거_슬롯으로_변경하면_PastReservationNotAllowedException() {
        // given: 같은 매장 매니저 + 과거 슬롯
        Reservation reservation = new Reservation(1L, 1L, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        Member sameStoreManager = new Member(
                4L, "manager-gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // when & then: 과거 시점 변경 불가
        assertThatThrownBy(() -> reservation.changeToByManager(sameStoreManager, pastDate, SAMPLE_TIME))
                .isInstanceOf(PastReservationNotAllowedException.class);
    }

    @Test
    void changeTo는_새_상태를_담은_Reservation을_반환한다() {
        // given: 본인 예약 + 새 날짜/시간
        long memberId = 1L;
        Reservation reservation = new Reservation(1L, memberId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);
        LocalDate futureDate = LocalDate.now().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(11, 0));

        // when: 변경 실행
        Reservation result = reservation.changeTo(memberId, futureDate, newTime);

        // then: 새 상태가 반영된 객체 반환
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDate()).isEqualTo(futureDate);
        assertThat(result.getTime().getId()).isEqualTo(2L);
    }

    @Test
    void create는_미래_슬롯이면_id가_null인_Reservation을_반환한다() {
        // given: 미래 날짜
        LocalDate futureDate = LocalDate.now().plusDays(2);

        // when: 정적 팩토리로 생성
        Reservation created = Reservation.create(1L, futureDate, SAMPLE_TIME, 1L, 1L);

        // then: id 는 null (영속 전 상태), 나머지 필드 반영
        assertThat(created.getId()).isNull();
        assertThat(created.getDate()).isEqualTo(futureDate);
    }

    @Test
    void create는_과거_슬롯이면_PastReservationNotAllowedException() {
        // given: 과거 날짜
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // when & then: 과거 시점 생성 불가
        assertThatThrownBy(() -> Reservation.create(1L, pastDate, SAMPLE_TIME, 1L, 1L))
                .isInstanceOf(PastReservationNotAllowedException.class);
    }

    @Test
    void 본인이_미래_예약을_취소하면_예외가_발생하지_않는다() {
        // given: 본인의 미래 예약
        long memberId = 1L;
        Reservation reservation = new Reservation(1L, memberId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);

        // when & then: 취소 성공
        assertThatCode(() -> reservation.cancelBy(memberId))
                .doesNotThrowAnyException();
    }

    @Test
    void 본인이_아닌_사용자가_취소하면_ReservationOwnerMismatchException() {
        // given: 다른 사용자가 본인 예약을 취소 시도
        long ownerId = 1L;
        long otherId = 2L;
        Reservation reservation = new Reservation(1L, ownerId, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);

        // when & then: 소유권 미스매치 예외
        assertThatThrownBy(() -> reservation.cancelBy(otherId))
                .isInstanceOf(ReservationOwnerMismatchException.class);
    }

    @Test
    void 본인이_과거_예약을_취소하면_PastReservationCancelNotAllowedException() {
        // given: 본인의 과거 예약
        long memberId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation reservation = new Reservation(1L, memberId, pastDate, SAMPLE_TIME, 1L, 1L);

        // when & then: 과거 예약 취소 불가
        assertThatThrownBy(() -> reservation.cancelBy(memberId))
                .isInstanceOf(PastReservationCancelNotAllowedException.class);
    }

    @Test
    void promoteTo는_새_소유자로_바뀐_Reservation을_반환한다() {
        // given: 원 소유자의 예약 + 새 소유자
        long originalOwner = 1L;
        long newOwner = 2L;
        Reservation reservation = new Reservation(1L, originalOwner, SAMPLE_DATE, SAMPLE_TIME, 1L, 1L);

        // when: 양도 실행
        Reservation promoted = reservation.promoteTo(newOwner);

        // then: memberId 만 바뀜, 나머지 동일
        assertThat(promoted.getId()).isEqualTo(1L);
        assertThat(promoted.getMemberId()).isEqualTo(newOwner);
        assertThat(promoted.getDate()).isEqualTo(SAMPLE_DATE);
    }
}
