package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.dto.request.ConfirmedReservationByCriteriaWebRequest;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.exception.ReservationSlotDuplicatedException;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ConfirmedReservationApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeAfterOneWeekDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private ConfirmedReservationApplicationService confirmedReservationApplicationService;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long timeId;
    private Long themeId;
    private Long memberId;
    private Long reservationId;

    private WaitingReservationApplicationService waitingReservationApplicationService;

    @BeforeEach
    void setUp() {
        ReservationSlotDataService reservationSlotDataService = new ReservationSlotDataService(
                reservationSlotRepository);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        ReservationDataService reservationDataService = new ReservationDataService(reservationRepository);
        ThemeDataService themeDataService = new ThemeDataService(themeRepository);
        ReservationTimeDataService reservationTimeDataService = new ReservationTimeDataService(
                reservationTimeRepository, reservationSlotDataService);
        confirmedReservationApplicationService = new ConfirmedReservationApplicationService(
                reservationSlotDataService,
                reservationTimeDataService, themeDataService,
                memberDataService, reservationDataService);
        waitingReservationApplicationService = new WaitingReservationApplicationService(reservationSlotDataService,
                memberDataService, reservationDataService);

        timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0))).getId();
        themeId = themeRepository.save(TestFixture.makeTheme()).getId();
        memberId = memberRepository.save(TestFixture.makeMember()).getId();
        reservationId = confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId, themeId, memberId,
                        afterOneHour)).id();
    }

    @Test
    void create_whenValidRequest_returnReservations() {
        // when
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(tomorrow, timeId, themeId, memberId,
                        afterOneHour));

        // then
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(null, null, tomorrow, tomorrow.plusDays(1)));

        ConfirmedReservationWebResponse response = result.getFirst();
        Assertions.assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(response.member().id()).isEqualTo(memberId),
                () -> assertThat(response.date()).isEqualTo(tomorrow),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(9, 0))
        );
    }

    @Test
    void create_whenDuplicateTimeSlot_throwsReservationSlotDuplicatedException() {
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(
                        new ConfirmedReservationCreateRequest(futureDate, timeId, themeId, memberId, afterOneHour)))
                .isInstanceOf(ReservationSlotDuplicatedException.class)
                .hasMessageContaining("해당 시간에 이미 예약 슬롯이 존재합니다.");
    }

    @Test
    void create_whenTimeIdNotFound_throwsReservationTimeNotFoundException() {
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(
                        new ConfirmedReservationCreateRequest(futureDate, 999L, themeId, memberId,
                                afterOneHour)))
                .isInstanceOf(ReservationTimeNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }

    @Test
    void create_whenThemeIdNotFound_throwsThemeNotFoundException() {
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(
                        new ConfirmedReservationCreateRequest(futureDate, timeId, 999L, memberId,
                                afterOneHour)))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 테마 정보가 없습니다.");
    }

    @Test
    void create_whenMemberIdNotFound_throwsMemberNotFoundException() {
        assertThatThrownBy(
                () -> waitingReservationApplicationService.create(
                        new WaitingReservationCreateRequest(futureDate, timeId, themeId, 999L)))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("존재하지 않은 멤버입니다.");
    }

    @Test
    void findByCriteria_whenValidRequest_returnsReservation() {
        // when
        Long themeId2 = themeRepository.save(new Theme("논리", "논리 게임 with Danny", "image.png")).getId();
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId, themeId2, memberId, afterOneHour));

        // then
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(themeId2, null, null, null));

        Assertions.assertAll(
                () -> assertThat(response.member().name()).isEqualTo("Mint"),
                () -> assertThat(response.date()).isEqualTo(futureDate),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(9, 0)),
                () -> assertThat(result).hasSize(1)
        );
    }

    @Test
    void findByCriteria_whenNoCondition_returnAllReservations() {
        // given
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId2, themeId, memberId,
                        afterOneHour));
        // when
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(null, null, null, null));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void findByCriteria_whenHasCondition_shouldReturnFilteredReservations() {
        // given
        Long themeId2 = themeRepository.save(new Theme("논리", "논리 게임 with Danny", "image.png")).getId();
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId, themeId2, memberId, afterOneHour));

        // when
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(themeId2, null, null, null));

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void cancel_whenReservationExists_removesSuccessfully() {
        // given

        // when
        confirmedReservationApplicationService.cancel(reservationId);

        // then
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(themeId, memberId, futureDate, futureDate.plusDays(1)));
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isEmpty();
            softAssertions.assertThat(reservationSlotRepository.findById(reservationId)).isEmpty();
        });
    }

    @Test
    void findMyReservations_shouldReturnMemberReservations() {
        // given
        Long themeId2 = themeRepository.save(new Theme("논리", "논리 게임 with Danny", "image.png")).getId();
        Long memberId2 = memberRepository.save(new Member("free", "free@gmail.com", "password", MemberRole.REGULAR))
                .getId();
        confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId, themeId2, memberId2,
                        afterOneHour));

        // when
        List<MyReservationResponse> result = confirmedReservationApplicationService.findMyReservations(memberId);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
                    softAssertions.assertThat(result).hasSize(1);
                    softAssertions.assertThat(result.getFirst().theme()).isEqualTo("추리");
                }
        );
    }

    @Test
    void findMyReservations_whenMemberIdNotFound_throwsMemberNotFoundException() {
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.findMyReservations(999L))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("존재하지 않은 멤버입니다.");
    }
}
