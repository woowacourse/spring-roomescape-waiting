package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.reservation.domain.BookingState;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationRepositoryImplTest {

    @Autowired
    ReservationRepositoryImpl reservationRepository;

    @Autowired
    MemberCommandRepository memberCommandRepository;

    @Autowired
    ThemeCommandRepository themeCommandRepository;

    @Autowired
    ReservationTimeCommandRepository reservationTimeCommandRepository;

    @Autowired
    ReservationCommandRepository reservationCommandRepository;

    @Test
    void ID_값에_해당하는_예약을_반환한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation saved = reservationCommandRepository.save(
                Reservation.createForRegister(date, time, theme, member,
                        BookingState.WAITING));

        final Long savedId = saved.getId();

        // when
        final Reservation found = reservationRepository.getByIdOrThrow(savedId);

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(savedId),
                () -> assertThat(found.getDate()).isEqualTo(date),
                () -> assertThat(found.getMember().getName()).isEqualTo("헤일러"),
                () -> assertThat(found.getTheme().getDescription()).isEqualTo("테마1 설명"),
                () -> assertThat(found.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(found.getState()).isEqualTo(BookingState.WAITING)
        );
    }

    @Test
    void ID_값에_해당하는_예약이_없으면_예외가_발생한다() {
        // given
        final Long id = 1L;

        // when & then
        assertThatThrownBy(() -> reservationRepository.getByIdOrThrow(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 예약을 찾을 수 없습니다.");
    }

    @MethodSource
    @ParameterizedTest
    void 테마ID_사용자ID_날짜_범위에_해당하는_예약_목록을_조회한다(final LocalDate from, final LocalDate to, final List<LocalTime> times,
                                            final List<LocalDate> dates) {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());

        final LocalDate date2 = LocalDate.now().plusDays(5);
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());

        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        reservationCommandRepository.save(
                Reservation.createForRegister(date1, time1, theme, member, BookingState.WAITING));

        reservationCommandRepository.save(
                Reservation.createForRegister(date2, time2, theme, member, BookingState.WAITING));

        // when
        final List<Reservation> found = reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                theme.getId(), member.getId(), from, to);

        // then
        assertAll(
                () -> assertThat(found).extracting(Reservation::getDate)
                        .containsExactlyInAnyOrderElementsOf(dates),
                () -> assertThat(found).extracting(reservation -> reservation.getTime().getStartAt())
                        .containsExactlyInAnyOrderElementsOf(times)
        );

    }

    static Stream<Arguments> 테마ID_사용자ID_날짜_범위에_해당하는_예약_목록을_조회한다() {
        return Stream.of(
                Arguments.of(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(5),
                        List.of(
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0)
                        ),
                        List.of(
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(5)
                        )
                ),
                Arguments.of(
                        LocalDate.now().plusDays(2),
                        LocalDate.now().plusDays(4),
                        List.of(
                        ),
                        List.of(
                        )
                ),
                Arguments.of(
                        LocalDate.now().plusDays(2),
                        LocalDate.now().plusDays(5),
                        List.of(
                                LocalTime.of(11, 0)
                        ),
                        List.of(
                                LocalDate.now().plusDays(5)
                        )
                ),
                Arguments.of(
                        LocalDate.now().minusDays(3),
                        LocalDate.now().plusDays(1),
                        List.of(
                                LocalTime.of(10, 0)
                        ),
                        List.of(
                                LocalDate.now().plusDays(1)
                        )
                )
        );
    }
}
