package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_ADMIN_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_2;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationSlotRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AdminCreateReservationRequest;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.MemberCreateReservationRequest;
import roomescape.reservation.ui.dto.response.AdminReservationResponse;
import roomescape.reservation.ui.dto.response.AdminReservationWaitingResponse;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.MemberReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    Clock clock;

    @Test
    void 사용자_예약_생성_시_이전_예약이_존재하지_않으면_예약_상태로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now(clock).plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when
        final MemberReservationResponse response = reservationService.createForMember(request, memberAuthInfo.id());

        // then
        assertAll(() -> assertThat(response.theme()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.rank()).isEqualTo(0L));
    }

    @Test
    void 사용자_예약_생성_시_이전_예약이_존재하면_대기_상태로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now(clock).plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        reservationService.createForMember(request, member1.getId());

        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());

        // when
        final MemberReservationResponse response = reservationService.createForMember(request, member2.getId());

        // then
        assertAll(() -> assertThat(response.theme()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.rank()).isEqualTo(1L));
    }

    @Test
    void 관리자_권한으로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now(clock).plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        final AdminCreateReservationRequest request = new AdminCreateReservationRequest(member.getId(), date,
                time.getId(), theme.getId());

        // when
        final AdminReservationResponse response = reservationService.createForAdmin(request);

        // then
        assertAll(() -> assertThat(response.theme().name()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time().startAt()).isEqualTo(time.getStartAt()));
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final Long reservationId = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme.getId()), member.getId()).id();

        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when
        reservationService.deleteReservation(reservationId, memberAuthInfo);

        // then
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }

    @Test
    void 본인의_예약이_아니면_삭제할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final ReservationSlot reservationSlot = reservationSlotRepository.save(
                new ReservationSlot(date, time, theme, LocalDateTime.now(clock)));

        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation savedReservation = reservationRepository.save(new Reservation(member1, reservationSlot));

        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());
        final MemberAuthInfo member2AuthInfo = new MemberAuthInfo(member2.getId(), member2.getRole());

        // when & then
        assertThatThrownBy(
                () -> reservationService.deleteReservation(savedReservation.getId(), member2AuthInfo)).isInstanceOf(
                AuthorizationException.class).hasMessage("삭제할 권한이 없습니다.");
    }

    @Test
    void 관리자는_모든_예약을_삭제할_수_있다() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final MemberReservationResponse response = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme.getId()), member.getId());

        final Member admin = memberRepository.save(NOT_SAVED_ADMIN_1());
        final MemberAuthInfo adminAuthInfo = new MemberAuthInfo(admin.getId(), admin.getRole());

        // when
        reservationService.deleteReservation(response.id(), adminAuthInfo);

        // then
        assertThat(reservationRepository.findById(response.id())).isEmpty();
    }

    @Test
    void 예약을_삭제하면_우선순위의_예약_대기가_예약_상태로_변경된다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());

        final Long confirmedReservationId = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme.getId()), member1.getId()).id();

        final Long waitingReservationId = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme.getId()), member2.getId()).id();

        final MemberAuthInfo member1AuthInfo = new MemberAuthInfo(member1.getId(), member1.getRole());

        // when
        reservationService.deleteReservation(confirmedReservationId, member1AuthInfo);

        // then
        final Reservation confirmedReservation = reservationRepository.getById(waitingReservationId);
        assertAll(() -> assertThat(reservationRepository.findById(confirmedReservationId)).isEmpty(),
                () -> assertThat(confirmedReservation.getId()).isEqualTo(waitingReservationId));
    }

    @Test
    void 예약_목록_전체를_조회한다() {
        // given
        final int beforeCount = reservationService.findAll().size();
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());
        reservationService.createForMember(new MemberCreateReservationRequest(date1, time1.getId(), theme1.getId()),
                member.getId());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());
        reservationService.createForMember(new MemberCreateReservationRequest(date2, time2.getId(), theme2.getId()),
                member.getId());

        // when
        final List<Reservation> reservations = reservationRepository.findAll();
        final int afterCount = reservations.size();
        final List<ReservationSlot> reservationSlots = reservations.stream().map(Reservation::getReservationSlot)
                .toList();

        // then
        assertAll(() -> assertThat(afterCount - beforeCount).isEqualTo(2),
                () -> assertThat(reservations).extracting(Reservation::getMember)
                        .containsExactlyInAnyOrder(member, member),
                () -> assertThat(reservationSlots).extracting(slot -> slot.getTime().getStartAt())
                        .containsExactlyInAnyOrder(time1.getStartAt(), time2.getStartAt()),
                () -> assertThat(reservationSlots).extracting(slot -> slot.getTheme().getName())
                        .containsExactlyInAnyOrder(theme1.getName(), theme2.getName()));
    }

    @Test
    void 특정_날짜에_특정_테마에_대해_이용_가능한_예약_시간_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(date, theme.getId());
        final List<AvailableReservationTimeResponse> before = reservationService.findAvailableReservationTimes(request);

        reservationService.createForMember(new MemberCreateReservationRequest(date, time1.getId(), theme.getId()),
                member.getId());
        reservationService.createForMember(new MemberCreateReservationRequest(date, time2.getId(), theme.getId()),
                member.getId());

        // when
        final List<AvailableReservationTimeResponse> after = reservationService.findAvailableReservationTimes(request);

        // then
        assertAll(() -> assertThat(before).extracting(AvailableReservationTimeResponse::alreadyBooked)
                        .containsExactlyInAnyOrder(false, false),
                () -> assertThat(after).extracting(AvailableReservationTimeResponse::alreadyBooked)
                        .containsExactlyInAnyOrder(true, true),
                () -> assertThat(after).extracting(AvailableReservationTimeResponse::startAt)
                        .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    void 예약을_추가할_때_이미_예약된_시간이면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);

        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        reservationService.createForMember(new MemberCreateReservationRequest(date, time.getId(), theme.getId()),
                member.getId());

        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertThatThrownBy(() -> reservationService.createForMember(request, memberAuthInfo.id())).isInstanceOf(
                AlreadyExistException.class).hasMessage("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
    }

    @Test
    void 한_테마의_날짜와_시간이_중복되는_예약을_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        reservationService.createForMember(new MemberCreateReservationRequest(date, time.getId(), theme.getId()),
                member.getId());

        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertThatThrownBy(() -> reservationService.createForMember(request, memberAuthInfo.id())).isInstanceOf(
                AlreadyExistException.class).hasMessage("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
    }

    @Test
    void 특정_회원의_예약_목록을_조회한다() {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());

        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final Long reservationId1 = reservationService.createForMember(
                new MemberCreateReservationRequest(date1, time1.getId(), theme1.getId()), member.getId()).id();
        final Long reservationId2 = reservationService.createForMember(
                new MemberCreateReservationRequest(date2, time2.getId(), theme2.getId()), member.getId()).id();

        // when
        final List<MemberReservationResponse> founds = reservationService.findReservationsByMemberId(member.getId());

        // then
        assertAll(() -> assertThat(founds).hasSize(2), () -> assertThat(founds).containsExactlyInAnyOrder(
                new MemberReservationResponse(reservationId1, theme1.getName(), date1, time1.getStartAt(), 0L),
                new MemberReservationResponse(reservationId2, theme2.getName(), date2, time2.getStartAt(), 0L)));
    }

    @Test
    void 예약_대기_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());
        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());

        reservationService.createForMember(new MemberCreateReservationRequest(date, time.getId(), theme1.getId()),
                member1.getId());
        final Long waitingReservationId1 = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme1.getId()), member2.getId()).id();
        reservationService.createForMember(new MemberCreateReservationRequest(date, time.getId(), theme2.getId()),
                member2.getId());
        final Long waitingReservationId2 = reservationService.createForMember(
                new MemberCreateReservationRequest(date, time.getId(), theme2.getId()), member1.getId()).id();

        // when
        final List<AdminReservationWaitingResponse> responses = reservationService.findReservationWaitings();

        // then
        assertAll(() -> assertThat(responses).hasSize(2),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::reservationId)
                        .containsExactlyInAnyOrder(waitingReservationId1, waitingReservationId2),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::memberName)
                        .containsExactlyInAnyOrder(member1.getName(), member2.getName()),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::reservationDate)
                        .containsExactlyInAnyOrder(date, date));
    }
}
