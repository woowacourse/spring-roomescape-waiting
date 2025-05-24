package roomescape.reservation.domain;

import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.MemberFixture.notSavedMember2;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime2;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme2;
import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 테마_회원_날짜_간격을_기준으로_예약을_조회한다() {
        // given
        final Member member1 = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        final Theme theme1 = themeRepository.save(notSavedTheme1());
        final Theme theme2 = themeRepository.save(notSavedTheme2());

        final ReservationTime time1 = reservationTimeRepository.save(notSavedReservationTime1());
        final ReservationTime time2 = reservationTimeRepository.save(notSavedReservationTime2());

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);
        final LocalDate tomorrow = today.plusDays(1);
        final LocalDate dayAfterTomorrow = today.plusDays(2);

        // member1, theme1, yesterday~tomorrow 예약
        final Reservation reservation1 = reservationRepository.save(
                new Reservation(yesterday, time1, theme1, member1, BOOKED));
        final Reservation reservation2 = reservationRepository.save(
                new Reservation(today, time2, theme1, member1, BOOKED));
        final Reservation reservation3 = reservationRepository.save(
                new Reservation(tomorrow, time1, theme1, member1, BOOKED));

        // member1, theme1, 날짜 범위 밖(dayAfterTomorrow) 예약
        reservationRepository.save(
                new Reservation(dayAfterTomorrow, time1, theme1, member1, BOOKED));

        // member2, theme1 예약
        reservationRepository.save(
                new Reservation(today, time1, theme1, member2, BOOKED));

        // member1, theme2 예약
        reservationRepository.save(
                new Reservation(today, time1, theme2, member1, BOOKED));

        // when
        final List<Reservation> founds = reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                theme1.getId(), member1.getId(), yesterday, tomorrow);

        // then
        SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(founds).hasSize(3);
                    softly.assertThat(founds.stream()
                                    .map(Reservation::getId)
                                    .toList())
                            .containsExactlyInAnyOrder(reservation1.getId(), reservation2.getId(), reservation3.getId());
                }
        );
    }
}
