package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_3;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_2;

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
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Test
    void ID_값에_해당하는_예약을_반환한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        final ReservationSlot reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));

        final Reservation saved = reservationRepository.save(new Reservation(member, reservationSlot));

        // when
        final Reservation found = reservationRepository.getByIdOrThrow(saved.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.getReservationSlot()).isEqualTo(saved.getReservationSlot()),
                () -> assertThat(found.getMember()).isEqualTo(saved.getMember())
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

    @Test
    void 사용자가_이미_예약한_정보의_존재_여부를_반환한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());
        final ReservationSlot reservationSlot = new ReservationSlot(date, time, theme1);
        final ReservationSlot notReservedSlot = new ReservationSlot(date, time, theme2);

        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        reservationSlot.addReservation(new Reservation(member, reservationSlot));

        reservationSlotRepository.save(reservationSlot);
        reservationSlotRepository.save(notReservedSlot);

        // when
        final Boolean reservedResult = reservationRepository.existsByReservationSlotAndMemberId(reservationSlot,
                member.getId());
        final Boolean notReservedResult = reservationRepository.existsByReservationSlotAndMemberId(notReservedSlot,
                member.getId());

        // then
        assertAll(
                () -> assertThat(reservedResult).isTrue(),
                () -> assertThat(notReservedResult).isFalse()
        );
    }

    @Test
    void 사용자의_예약_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());

        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final ReservationSlot reservationSlot1 = new ReservationSlot(date, time1, theme1);
        reservationSlot1.addReservation(new Reservation(member, reservationSlot1));

        final ReservationSlot reservationSlot2 = new ReservationSlot(date, time2, theme1);
        reservationSlot2.addReservation(new Reservation(member, reservationSlot2));

        final ReservationSlot reservationSlot3 = new ReservationSlot(date, time2, theme2);
        reservationSlot3.addReservation(new Reservation(member, reservationSlot3));

        reservationSlotRepository.save(reservationSlot1);
        reservationSlotRepository.save(reservationSlot2);
        reservationSlotRepository.save(reservationSlot3);

        // when
        final List<Reservation> reservations = reservationRepository.findAllByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(reservations).extracting(Reservation::getReservationSlot)
                        .containsExactlyInAnyOrder(reservationSlot1, reservationSlot2, reservationSlot3),
                () -> assertThat(reservations).extracting(Reservation::getMember)
                        .containsExactlyInAnyOrder(member, member, member)
        );
    }

    @MethodSource
    @ParameterizedTest
    void 테마ID_사용자ID_날짜_범위에_해당하는_예약_목록을_조회한다(final LocalDate from, final LocalDate to, final List<LocalTime> times,
                                            final List<LocalDate> dates) {
        // given
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final ReservationSlot reservationSlot1 = reservationSlotRepository.save(
                new ReservationSlot(date1, time1, theme));

        final LocalDate date2 = LocalDate.now().plusDays(5);
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final ReservationSlot reservationSlot2 = reservationSlotRepository.save(
                new ReservationSlot(date2, time2, theme));

        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        reservationRepository.save(new Reservation(member, reservationSlot1));
        reservationRepository.save(new Reservation(member, reservationSlot2));

        // when
        final List<Reservation> found = reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                theme.getId(), member.getId(), from, to);

        // then
        assertAll(
                () -> assertThat(found).extracting(reservation -> reservation.getReservationSlot().getDate())
                        .containsExactlyInAnyOrderElementsOf(dates),
                () -> assertThat(found).extracting(
                                reservation -> reservation.getReservationSlot().getTime().getStartAt())
                        .containsExactlyInAnyOrderElementsOf(times)
        );

    }

    @Test
    void 해당_예약의_대기_순번을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final ReservationSlot reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));

        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());
        final Member member3 = memberRepository.save(NOT_SAVED_MEMBER_3());

        reservationRepository.save(new Reservation(member1, reservationSlot));
        final Reservation target = reservationRepository.save(new Reservation(member2, reservationSlot));
        reservationRepository.save(new Reservation(member3, reservationSlot));

        // when
        final Long rank = reservationRepository.getReservationRankById(target.getId());

        // then
        assertThat(rank).isEqualTo(2);
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
