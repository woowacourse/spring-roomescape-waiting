package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_ADMIN_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_2;

import java.time.LocalDate;
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
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
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
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 사용자_예약_생성_시_이전_예약이_존재하지_않으면_예약_상태로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when
        MemberReservationResponse response = reservationService.createForMember(request, memberAuthInfo.id());

        // then
        assertAll(
                () -> assertThat(response.theme()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED.getDescription()),
                () -> assertThat(response.rank()).isEqualTo(1L)
        );
    }

    @Test
    void 사용자_예약_생성_시_이전_예약이_존재하면_대기_상태로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
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
        assertAll(
                () -> assertThat(response.theme()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.status()).isEqualTo(BookingStatus.WAITING.getDescription()),
                () -> assertThat(response.rank()).isEqualTo(2L)
        );
    }

    @Test
    void 관리자_권한으로_예약을_생성한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());
        final BookingStatus status = BookingStatus.WAITING;

        final AdminCreateReservationRequest request = new AdminCreateReservationRequest(member.getId(), date,
                time.getId(), theme.getId(), status);

        // when
        final AdminReservationResponse response = reservationService.createForAdmin(request);

        // then
        assertAll(
                () -> assertThat(response.theme().name()).isEqualTo(theme.getName()),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time().startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.status()).isEqualTo(status.getDescription())
        );
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final Reservation reservation = Reservation.createForRegister(date, time, theme, member,
                BookingStatus.CONFIRMED);
        final Reservation saved = reservationRepository.save(reservation);
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertAll(
                () -> assertThatCode(() -> reservationService.deleteReservation(saved.getId(), memberAuthInfo))
                        .doesNotThrowAnyException(),
                () -> assertThatThrownBy(() -> reservationRepository.getByIdOrThrow(saved.getId()))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("해당 예약을 찾을 수 없습니다.")
        );
    }

    @Test
    void 본인의_예약이_아니면_삭제할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation reservation = Reservation.createForRegister(date, time, theme, member1,
                BookingStatus.CONFIRMED);
        final Reservation savedReservation = reservationRepository.save(reservation);

        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());
        final MemberAuthInfo member2AuthInfo = new MemberAuthInfo(member2.getId(), member2.getRole());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(savedReservation.getId(), member2AuthInfo))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("삭제할 권한이 없습니다.");
    }

    @Test
    void 관리자는_모든_예약을_삭제할_수_있다() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());

        final Member member
                = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation reservation = Reservation.createForRegister(date, time, theme, member,
                BookingStatus.CONFIRMED);
        final Reservation savedReservation = reservationRepository.save(reservation);

        final Member admin = memberRepository.save(NOT_SAVED_ADMIN_1());
        final MemberAuthInfo adminAuthInfo = new MemberAuthInfo(admin.getId(), admin.getRole());

        // when & then
        assertAll(
                () -> assertThatCode(
                        () -> reservationService.deleteReservation(savedReservation.getId(), adminAuthInfo))
                        .doesNotThrowAnyException(),
                () -> assertThatThrownBy(() -> reservationRepository.getByIdOrThrow(savedReservation.getId()))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("해당 예약을 찾을 수 없습니다.")
        );
    }

    @Test
    void 예약을_삭제하면_우선순위의_예약_대기가_예약_상태로_변경된다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());

        final Member member1 = memberRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation reservation1 = Reservation.createForRegister(date, time, theme, member1,
                BookingStatus.CONFIRMED);
        final Reservation confirmedReservation = reservationRepository.save(reservation1);

        final Member member2 = memberRepository.save(NOT_SAVED_MEMBER_2());
        Reservation reservation2 = Reservation.createForRegister(date, time, theme, member2, BookingStatus.WAITING);
        final Reservation waitingReservation = reservationRepository.save(reservation2);

        final MemberAuthInfo member1AuthInfo = new MemberAuthInfo(member1.getId(), member1.getRole());

        // when
        reservationService.deleteReservation(confirmedReservation.getId(), member1AuthInfo);

        assertAll(
                () -> assertThat(reservationRepository.findById(confirmedReservation.getId())).isEmpty(),
                () -> assertThat(waitingReservation.getStatus()).isEqualTo(BookingStatus.CONFIRMED)
        );
    }

    @Test
    void 예약_목록_전체를_조회한다() {
        // given
        final int beforeCount = reservationService.findAll().size();
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeRepository.save(NOT_SAVED_THEME_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeRepository.save(NOT_SAVED_THEME_2());

        final Reservation saved1 = reservationRepository.save(
                Reservation.createForRegister(date1, time1, theme1, member, BookingStatus.CONFIRMED)
        );
        final Reservation saved2 = reservationRepository.save(
                Reservation.createForRegister(date2, time2, theme2, member, BookingStatus.CONFIRMED)
        );

        // when
        final int afterCount = reservationService.findAll().size();

        // then
        assertAll(
                () -> assertThat(afterCount - beforeCount).isEqualTo(2),
                () -> assertThat(reservationRepository.getByIdOrThrow(saved1.getId()).getDate()).isEqualTo(date1),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved1.getId()).getTheme().getName()).isEqualTo(
                        "테마1"),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved1.getId()).getMember().getName()).isEqualTo(
                        "헤일러"),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved1.getId()).getTime().getStartAt()).isEqualTo(
                        LocalTime.of(10, 0)),
                () -> assertThat(reservationRepository.getByIdOrThrow(saved2.getId()).getDate()).isEqualTo(date2),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved2.getId()).getTheme().getName()).isEqualTo(
                        "테마2"),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved2.getId()).getMember().getName()).isEqualTo(
                        "헤일러"),
                () -> assertThat(
                        reservationRepository.getByIdOrThrow(saved2.getId()).getTime().getStartAt()).isEqualTo(
                        LocalTime.of(11, 0))
        );
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

        final List<AvailableReservationTimeResponse> before = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .toList();

        reservationRepository.save(
                Reservation.createForRegister(date, time1, theme, member, BookingStatus.CONFIRMED));
        reservationRepository.save(
                Reservation.createForRegister(date, time2, theme, member, BookingStatus.CONFIRMED));

        // when
        final List<AvailableReservationTimeResponse> after = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .toList();

        // then
        assertAll(
                () -> assertThat(after.size() - before.size()).isEqualTo(2),
                () -> assertThat(after).extracting(AvailableReservationTimeResponse::startAt)
                        .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0))
        );
    }

    @Test
    void 예약을_추가할_때_이미_예약된_시간이면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);

        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        reservationRepository.save(
                Reservation.createForRegister(date, time, theme, member, BookingStatus.CONFIRMED));

        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertThatThrownBy(() -> reservationService.createForMember(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
    }

    @Test
    void 한_테마의_날짜와_시간이_중복되는_예약을_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberRepository.save(NOT_SAVED_MEMBER_1());

        final MemberCreateReservationRequest request = new MemberCreateReservationRequest(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        reservationRepository.save(
                Reservation.createForRegister(date, time, theme, member, BookingStatus.CONFIRMED));

        // when & then
        assertThatThrownBy(() -> reservationService.createForMember(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
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

        final Reservation saved1 = reservationRepository.save(
                Reservation.createForRegister(date1, time1, theme1, member, BookingStatus.WAITING)
        );

        final Reservation saved2 = reservationRepository.save(
                Reservation.createForRegister(date2, time2, theme2, member, BookingStatus.WAITING)
        );

        // when
        final List<MemberReservationResponse> founds = reservationService.findReservationsByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(founds).hasSize(2),
                () -> assertThat(founds)
                        .containsExactlyInAnyOrder(
                                new MemberReservationResponse(saved1.getId(), "테마1", LocalDate.now().plusDays(1),
                                        LocalTime.of(10, 0),
                                        "대기",
                                        1L),
                                new MemberReservationResponse(saved2.getId(), "테마2", LocalDate.now().plusDays(2),
                                        LocalTime.of(11, 0),
                                        "대기",
                                        1L)
                        )
        );
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

        reservationRepository.save(
                Reservation.createForRegister(date, time, theme1, member1, BookingStatus.CONFIRMED)
        );
        Reservation waitingReservation1 = reservationRepository.save(
                Reservation.createForRegister(date, time, theme1, member2, BookingStatus.WAITING)
        );

        reservationRepository.save(
                Reservation.createForRegister(date, time, theme2, member2, BookingStatus.CONFIRMED)
        );
        Reservation waitingReservation2 = reservationRepository.save(
                Reservation.createForRegister(date, time, theme2, member1, BookingStatus.WAITING)
        );

        // when
        List<AdminReservationWaitingResponse> responses = reservationService.findReservationWaitings();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::reservationId)
                        .containsExactlyInAnyOrder(waitingReservation1.getId(), waitingReservation2.getId()),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::memberName)
                        .containsExactlyInAnyOrder(member1.getName(), member2.getName()),
                () -> assertThat(responses).extracting(AdminReservationWaitingResponse::reservationDate)
                        .containsExactlyInAnyOrder(date, date)
        );
    }
}
