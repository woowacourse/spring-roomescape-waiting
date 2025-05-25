package roomescape.reservation.application;

import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.MemberFixture.notSavedMember2;
import static roomescape.fixture.domain.MemberFixture.notSavedMember3;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.WaitingResponse;
import roomescape.reservation.ui.dto.response.WaitingWithRankResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@DisplayName("회원 예약 대기 관리 서비스")
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 예약_대기를_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationSlot slot = ReservationSlot.of(date, time, theme);

        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        createBookedReservation(slot, member2); // member2 예약 추가

        final CreateWaitingRequest.ForMember request =
                new CreateWaitingRequest.ForMember(date, time.getId(), theme.getId());

        // when
        final WaitingResponse waitingResponse = waitingService.create(request, member.getId()); // member1 예약 대기 추가

        // then
        SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(waitingResponse.date()).isEqualTo(date);
                    softly.assertThat(waitingResponse.time().id()).isEqualTo(time.getId());
                    softly.assertThat(waitingResponse.theme().id()).isEqualTo(theme.getId());
                    softly.assertThat(waitingResponse.member().id()).isEqualTo(member.getId());
                }
        );
    }

    @Test
    void 예약이_없는_시간에_예약_대기를_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        final CreateWaitingRequest.ForMember request =
                new CreateWaitingRequest.ForMember(date, time.getId(), theme.getId());

        // when & then
        Assertions.assertThatThrownBy(() -> waitingService.create(request, member.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 본인의_예약이_이미_있는_경우_예약_대기를_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationSlot slot = ReservationSlot.of(date, time, theme);

        final Member member = memberRepository.save(notSavedMember1());

        createBookedReservation(slot, member); // member 예약 추가

        final CreateWaitingRequest.ForMember request =
                new CreateWaitingRequest.ForMember(date, time.getId(), theme.getId());

        // when & then
        Assertions.assertThatThrownBy(() -> waitingService.create(request, member.getId()))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 본인의_예약_대기가_이미_있는_경우_예약_대기를_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationSlot slot = ReservationSlot.of(date, time, theme);

        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        createBookedReservation(slot, member2); // member2 예약 추가

        final CreateWaitingRequest.ForMember request =
                new CreateWaitingRequest.ForMember(date, time.getId(), theme.getId());
        waitingService.create(request, member.getId()); // member1 예약 대기 추가

        // when & then
        Assertions.assertThatThrownBy(() -> waitingService.create(request, member.getId()))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 예약_대기를_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationSlot slot = ReservationSlot.of(date, time, theme);

        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());
        final LocalDateTime waitingRequestTime = LocalDateTime.now();

        createBookedReservation(slot, member2); // member2 예약 추가
        final Waiting waiting = createWaiting(slot, member, waitingRequestTime); // member1 예약 대기 추가

        // when
        waitingService.deleteIfOwner(waiting.getId(), member.getId()); // member1 예약 대기 삭제

        // then
        Assertions.assertThat(waitingRepository.findById(waiting.getId()).isEmpty())
                .isTrue();
    }

    @Test
    void 삭제하려는_예약_대기가_존재하지_않는_경우_예외가_발생한다() {
        // given
        final Member member = memberRepository.save(notSavedMember1());
        final Long notExistWaitingId = Long.MAX_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> waitingService.deleteIfOwner(notExistWaitingId, member.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 본인의_것이_아닌_예약_대기를_삭제하려_하면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final ReservationSlot slot = ReservationSlot.of(date, time, theme);

        final Member member2 = memberRepository.save(notSavedMember2());
        final Member member3 = memberRepository.save(notSavedMember3());
        final LocalDateTime waitingRequestTime = LocalDateTime.now();

        createBookedReservation(slot, member2); // member2 예약 추가
        final Waiting waiting = createWaiting(slot, member, waitingRequestTime); // member1 예약 대기 추가

        // when & then
        Assertions.assertThatThrownBy(() -> waitingService.deleteIfOwner(waiting.getId(), member3.getId()))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void 특정_회원의_예약_대기_목록을_조회한다() {
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
        List<WaitingWithRankResponse.ForMember> waitingWithRanksOfMember2 = waitingService.findAllWaitingWithRankByMemberId(
                member2.getId());
        List<WaitingWithRankResponse.ForMember> waitingWithRanksOfMember3 = waitingService.findAllWaitingWithRankByMemberId(
                member3.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waitingWithRanksOfMember2).hasSize(3);
            softly.assertThat(waitingWithRanksOfMember3).hasSize(2);
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
