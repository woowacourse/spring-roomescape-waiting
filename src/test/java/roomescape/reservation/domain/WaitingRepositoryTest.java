package roomescape.reservation.domain;

import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.MemberFixture.notSavedMember2;
import static roomescape.fixture.domain.MemberFixture.notSavedMember3;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class WaitingRepositoryTest {

    @Autowired
    private roomescape.reservation.domain.WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 예약_대기_1순위를_찾아온다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalDate datePlus1 = date.plusDays(1);
        final LocalDate datePlus2 = date.plusDays(2);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());

        final ReservationSlot slot1 = ReservationSlot.of(date, time, theme);
        final ReservationSlot slot2 = ReservationSlot.of(datePlus1, time, theme);
        final ReservationSlot slot3 = ReservationSlot.of(datePlus2, time, theme);

        final Member member1 = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());
        final Member member3 = memberRepository.save(notSavedMember3());
        final LocalDateTime waitingRequestTime = LocalDateTime.now();

        createBookedReservation(slot1, member1);
        createBookedReservation(slot2, member1);
        createBookedReservation(slot3, member1);

        // slot1에 대해서는 member2, member3 순서로 예약 대기 추가
        // slot2에 대해서는 member3, member2 순서로 예약 대기 추가
        // slot3는 member2만 예약 대기 추가
        final List<Waiting> waitings = List.of(
                createWaiting(slot1, member2, waitingRequestTime),  // 1순위
                createWaiting(slot2, member2, waitingRequestTime.plusMinutes(1)), // 2순위
                createWaiting(slot3, member2, waitingRequestTime),  // 1순위

                createWaiting(slot1, member3, waitingRequestTime.plusMinutes(1)), // 2순위
                createWaiting(slot2, member3, waitingRequestTime)   // 1순위
        );

        // when
        final Waiting slot1First = waitingRepository.findFirstByReservationSlotOrderByCreatedAt(slot1).get();
        final Waiting slot2First = waitingRepository.findFirstByReservationSlotOrderByCreatedAt(slot2).get();
        final Waiting slot3First = waitingRepository.findFirstByReservationSlotOrderByCreatedAt(slot3).get();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(slot1First.getMember()).isEqualTo(member2);
            softly.assertThat(slot2First.getMember()).isEqualTo(member3);
            softly.assertThat(slot3First.getMember()).isEqualTo(member2);
        });
    }

    private Waiting createWaiting(
            final ReservationSlot slot,
            final Member member,
            final LocalDateTime waitingRequestTime
    ) {
        return waitingRepository.save(Waiting.of(slot, member, waitingRequestTime));
    }

    private void createBookedReservation(
            final ReservationSlot slot,
            final Member member
    ) {
        reservationRepository.save(Reservation.of(slot, member, BOOKED));
    }
}
