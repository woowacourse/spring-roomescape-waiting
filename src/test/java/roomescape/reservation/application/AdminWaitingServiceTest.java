package roomescape.reservation.application;

import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.MemberFixture.notSavedMember2;
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
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.fixture.domain.MemberFixture;
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
@DisplayName("관리자 예약 대기 관리 서비스")
class AdminWaitingServiceTest {

    @Autowired
    private AdminWaitingService adminWaitingService;

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
    void 특정_회원의_예약_대기를_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        createBookedReservation(date, time, theme, member2); // member2 예약 추가

        final CreateWaitingRequest request =
                new CreateWaitingRequest(date, time.getId(), theme.getId(), member.getId());

        // when
        final WaitingResponse waitingResponse = adminWaitingService.create(request); // member1 예약 대기 추가

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

        final CreateWaitingRequest request =
                new CreateWaitingRequest(date, time.getId(), theme.getId(), member.getId());

        // when & then
        Assertions.assertThatThrownBy(() -> adminWaitingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 동일한_회원의_예약이_이미_있는_경우_예약_대기를_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        createBookedReservation(date, time, theme, member); // member 예약 추가

        final CreateWaitingRequest request =
                new CreateWaitingRequest(date, time.getId(), theme.getId(), member.getId());

        // when & then
        Assertions.assertThatThrownBy(() -> adminWaitingService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 동일한_회원의_예약_대기가_이미_있는_경우_예약_대기를_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        createBookedReservation(date, time, theme, member2); // member2 예약 추가

        final CreateWaitingRequest request =
                new CreateWaitingRequest(date, time.getId(), theme.getId(), member.getId());
        adminWaitingService.create(request); // member1 예약 대기 추가

        // when & then
        Assertions.assertThatThrownBy(() -> adminWaitingService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 예약_대기를_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());
        final LocalDateTime waitingRequestTime = LocalDateTime.now();

        createBookedReservation(date, time, theme, member2); // member2 예약
        final Waiting waiting = createWaiting(date, time, theme, member, waitingRequestTime); // member1 예약 대기 추가

        // when
        adminWaitingService.deleteAsAdmin(waiting.getId()); // member1 예약 대기 삭제

        // then
        Assertions.assertThatThrownBy(() -> waitingRepository.getById(waiting.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 삭제하려는_예약_대기가_존재하지_않는_경우_예외가_발생한다() {
        // given
        final Long notExistWaitingId = Long.MAX_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> adminWaitingService.deleteAsAdmin(notExistWaitingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_대기_목록_전체를_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final List<Member> members = MemberFixture.notSavedMembers(6);
        for (final Member member : members) {
            memberRepository.save(member); // member 6명 추가
        }

        createBookedReservation(date, time, theme, members.get(0)); // 0번 member 예약 추가

        final LocalDateTime dateTimeNow = LocalDateTime.now();
        final List<Waiting> notSavedWaitings = List.of(
                Waiting.of(ReservationSlot.of(date, time, theme), members.get(1), dateTimeNow.minusDays(5)), // 대기 1순위
                Waiting.of(ReservationSlot.of(date, time, theme), members.get(3), dateTimeNow.minusDays(4)), // 대기 2순위
                Waiting.of(ReservationSlot.of(date, time, theme), members.get(2), dateTimeNow.minusDays(3)), // 대기 3순위
                Waiting.of(ReservationSlot.of(date, time, theme), members.get(5), dateTimeNow.minusDays(2)), // 대기 4순위
                Waiting.of(ReservationSlot.of(date, time, theme), members.get(4), dateTimeNow.minusDays(1))  // 대기 5순위
        );
        // 5개 예약 대기 추가
        for (final Waiting waiting : notSavedWaitings) {
            waitingRepository.save(waiting);
        }

        // when
        final List<WaitingWithRankResponse> waitingWithRanks = adminWaitingService.findAllWaitingWithRank();

        // then
        SoftAssertions.assertSoftly(soflty -> {
            soflty.assertThat(waitingWithRanks.get(0).member().id()).isEqualTo(members.get(1).getId());
            soflty.assertThat(waitingWithRanks.get(1).member().id()).isEqualTo(members.get(3).getId());
            soflty.assertThat(waitingWithRanks.get(2).member().id()).isEqualTo(members.get(2).getId());
            soflty.assertThat(waitingWithRanks.get(3).member().id()).isEqualTo(members.get(5).getId());
            soflty.assertThat(waitingWithRanks.get(4).member().id()).isEqualTo(members.get(4).getId());
        });
    }

    private Waiting createWaiting(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime waitingRequestTime
    ) {
        return waitingRepository.save(
                Waiting.of(ReservationSlot.of(date, time, theme), member, waitingRequestTime)
        );
    }

    private void createBookedReservation(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        reservationRepository.save(
                Reservation.of(ReservationSlot.of(date, time, theme), member, BOOKED)
        );
    }
}
