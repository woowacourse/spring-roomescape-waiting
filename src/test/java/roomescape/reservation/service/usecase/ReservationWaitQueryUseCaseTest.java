package roomescape.reservation.service.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.repository.FakeReservationWaitRepository;
import roomescape.reservation.repository.ReservationWaitRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;

class ReservationWaitQueryUseCaseTest {

    private ReservationWaitQueryUseCase reservationWaitQueryUseCase;
    private ReservationWaitRepository reservationWaitRepository;

    @BeforeEach
    void setUp() {
        reservationWaitRepository = new FakeReservationWaitRepository();
        reservationWaitQueryUseCase = new ReservationWaitQueryUseCase(reservationWaitRepository);
    }

    @DisplayName("date, timeId, themeId를 통해 index 번째의 예약 대기를 조회한다.")
    @Test
    void findByParamsAt() {
        // given
        final Member member1 = new Member(
                1L,
                MemberName.from("member1"),
                MemberEmail.from("11@gmail.com"),
                Role.USER
        );

        final Member member2 = new Member(
                2L,
                MemberName.from("member2"),
                MemberEmail.from("22@gmail.com"),
                Role.USER
        );

        final ReservationDate reservationDate = ReservationDate.from(LocalDate.MAX);
        final ReservationTime reservationTime = ReservationTime.withId(1L, LocalTime.MAX);

        final Theme theme = Theme.withId(
                1L,
                ThemeName.from("theme1"),
                ThemeDescription.from("theme1"),
                ThemeThumbnail.from("www.theme1.com")
        );

        final ReservationWait reservationWait1 = reservationWaitRepository.save(
                ReservationWait.withoutId(
                        member1,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        final ReservationWait reservationWait2 = reservationWaitRepository.save(
                ReservationWait.withoutId(
                        member2,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        // when
        final Optional<ReservationWait> actual = reservationWaitQueryUseCase.findByParamsAt(
                reservationDate,
                reservationTime.getId(),
                theme.getId(),
                1
        );

        // then
        assertAll(
                () -> assertThat(actual).isPresent(),
                () -> assertThat(actual.get()).isEqualTo(reservationWait2)
        );
    }

    @DisplayName("index가 음수일 경우 예외가 발생한다.")
    @Test
    void validateIndexPositive() {
        // when & then
        assertThatThrownBy(() -> reservationWaitQueryUseCase.findByParamsAt(
                ReservationDate.from(LocalDate.MAX),
                1L,
                1L,
                -1
        )).isInstanceOf(IllegalStateException.class);
    }
}
