package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_2;

import java.time.LocalDate;
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
import roomescape.fixture.domain.ReservationTimeFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationSlotRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void ID_값에_해당하는_테마를_반환한다() {
        // given
        final Theme saved = themeRepository.save(NOT_SAVED_THEME_1());

        // when
        final Theme found = themeRepository.getByIdOrThrow(saved.getId());

        // then
        assertAll(() -> assertThat(found.getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.getName()).isEqualTo(saved.getName()),
                () -> assertThat(found.getDescription()).isEqualTo(saved.getDescription()),
                () -> assertThat(found.getThumbnail()).isEqualTo(saved.getThumbnail()));
    }

    @Test
    void ID_값에_해당하는_테마가_없으면_예외가_발생한다() {
        // given
        final Long id = 99L;

        // when
        assertThatThrownBy(() -> themeRepository.getByIdOrThrow(id)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 테마가 존재하지 않습니다.");
    }

    @MethodSource
    @ParameterizedTest
    void 기간_내_예약이_많은_순서대로_테마를_조회한다(final LocalDate from, final LocalDate to, final int topN,
                                   final List<String> expectedNames) {
        // given
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());

        final ReservationTime time1 = reservationTimeRepository.save(
                ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1());
        final ReservationTime time2 = reservationTimeRepository.save(
                ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2());
        final ReservationTime time3 = reservationTimeRepository.save(
                ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_3());

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final LocalDate date2 = LocalDate.now().plusDays(5);

        ReservationSlot reservationSlot1 = reservationSlotRepository.save(new ReservationSlot(date1, time1, theme1));
        ReservationSlot reservationSlot2 = reservationSlotRepository.save(new ReservationSlot(date1, time2, theme2));
        ReservationSlot reservationSlot3 = reservationSlotRepository.save(new ReservationSlot(date1, time3, theme1));
        ReservationSlot reservationSlot4 = reservationSlotRepository.save(new ReservationSlot(date2, time1, theme2));
        ReservationSlot reservationSlot5 = reservationSlotRepository.save(new ReservationSlot(date2, time2, theme1));
        ReservationSlot reservationSlot6 = reservationSlotRepository.save(new ReservationSlot(date2, time2, theme2));
        ReservationSlot reservationSlot7 = reservationSlotRepository.save(new ReservationSlot(date2, time3, theme2));

        reservationRepository.save(new Reservation(member, reservationSlot1));
        reservationRepository.save(new Reservation(member, reservationSlot2));
        reservationRepository.save(new Reservation(member, reservationSlot3));
        reservationRepository.save(new Reservation(member, reservationSlot4));
        reservationRepository.save(new Reservation(member, reservationSlot5));
        reservationRepository.save(new Reservation(member, reservationSlot6));
        reservationRepository.save(new Reservation(member, reservationSlot7));

        // when
        final List<Theme> actual = themeRepository.getTopNThemesInPeriod(from, to, topN);

        // then
        assertThat(actual).extracting(Theme::getName).containsExactlyInAnyOrderElementsOf(expectedNames);
    }

    static Stream<Arguments> 기간_내_예약이_많은_순서대로_테마를_조회한다() {
        return Stream.of(
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 10, List.of("테마2", "테마1")),
                Arguments.of(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 10, List.of("테마2", "테마1")),
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 10, List.of("테마1", "테마2")),
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), 1, List.of("테마1")),
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 1, List.of("테마2")));
    }
}
