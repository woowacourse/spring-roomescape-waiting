package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.exception.NotFoundException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.fixture.FakeReservationTimeRepositoryFixture;
import roomescape.fixture.FakeThemeRepositoryFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("예약 요청 검증 테스트")
class ReservationCheckerTest {

    private final ReservationTimeRepository reservationTimeRepository = FakeReservationTimeRepositoryFixture.create();
    private final ThemeRepository themeRepository = FakeThemeRepositoryFixture.create();
    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final ReservationChecker reservationChecker = new ReservationChecker(reservationTimeRepository, themeRepository, memberRepository);

    @DisplayName("요청 받은 데이터로 빈 id의 예약 객체를 생성할 수 있다")
    @Test
    void createReservationTest() {
        // given
        LocalDate targetDate = LocalDate.now().plusDays(10);
        ReservationRequest request = new ReservationRequest(targetDate, 1L, 1L, 1L);

        // when
        Reservation reservation = reservationChecker.createReservationWithoutId(request);

        // then
        assertAll(
                () -> assertThat(reservation.getDate()).isEqualTo(targetDate),
                () -> assertThat(reservation.getName().getName()).isEqualTo("어드민"),
                () -> assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(reservation.getTheme().getName()).isEqualTo("우테코")
        );
    }

    @DisplayName("유효하지 않은 시간 id로 예약 생성을 요청하면 예외가 발생한다")
    @Test
    void createReservationExceptionTest1() {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(10), 100L, 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationChecker.createReservationWithoutId(request))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("유효하지 않은 테마 id로 예약 생성을 요청하면 예외가 발생한다")
    @Test
    void createReservationExceptionTest2() {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(10), 1L, 100L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationChecker.createReservationWithoutId(request))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("유효하지 않은 멤버 id로 예약 생성을 요청하면 예외가 발생한다")
    @Test
    void createReservationExceptionTest3() {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(10), 1L, 1L, 100L);

        // when & then
        assertThatThrownBy(() -> reservationChecker.createReservationWithoutId(request))
                .isInstanceOf(NotFoundException.class);
    }
}
