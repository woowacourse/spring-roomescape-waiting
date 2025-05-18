package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
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
import roomescape.member.domain.MemberCommandRepository;
import roomescape.reservation.domain.BookingState;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse.ForMember;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeCommandRepository reservationTimeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;
    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Test
    void 예약을_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());
        final CreateReservationRequest.ForMember request = new CreateReservationRequest.ForMember(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertThatCode(() -> reservationService.createForMember(request, memberAuthInfo.id()))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final Reservation reservation = Reservation.createForRegister(date, time, theme, member,
                BookingState.CONFIRMED);
        final Reservation saved = reservationCommandRepository.save(reservation);
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        assertAll(
                () -> assertThatCode(() -> reservationService.deleteIfOwner(saved.getId(), memberAuthInfo))
                        .doesNotThrowAnyException(),
                () -> assertThatThrownBy(() -> reservationQueryRepository.getByIdOrThrow(saved.getId()))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("해당 예약을 찾을 수 없습니다.")
        );
    }

    @Test
    void 본인의_예약이_아니면_삭제할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member1 = memberCommandRepository.save(NOT_SAVED_MEMBER_1());
        final Reservation reservation = Reservation.createForRegister(date, time, theme, member1,
                BookingState.CONFIRMED);
        final Reservation savedReservation = reservationCommandRepository.save(reservation);

        final Member member2 = memberCommandRepository.save(NOT_SAVED_MEMBER_2());
        final MemberAuthInfo member2AuthInfo = new MemberAuthInfo(member2.getId(), member2.getRole());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteIfOwner(savedReservation.getId(), member2AuthInfo))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("삭제할 권한이 없습니다.");
    }

    @Test
    void 예약_목록_전체를_조회한다() {
        // given
        final int beforeCount = reservationService.findAll().size();
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeCommandRepository.save(NOT_SAVED_THEME_2());

        final Reservation saved1 = reservationCommandRepository.save(
                Reservation.createForRegister(date1, time1, theme1, member, BookingState.CONFIRMED)
        );
        final Reservation saved2 = reservationCommandRepository.save(
                Reservation.createForRegister(date2, time2, theme2, member, BookingState.CONFIRMED)
        );

        // when
        final int afterCount = reservationService.findAll().size();

        // then
        assertAll(
                () -> assertThat(afterCount - beforeCount).isEqualTo(2),
                () -> assertThat(reservationQueryRepository.getByIdOrThrow(saved1.getId()).getDate()).isEqualTo(date1),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved1.getId()).getTheme().getName()).isEqualTo(
                        "테마1"),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved1.getId()).getMember().getName()).isEqualTo(
                        "헤일러"),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved1.getId()).getTime().getStartAt()).isEqualTo(
                        LocalTime.of(10, 0)),
                () -> assertThat(reservationQueryRepository.getByIdOrThrow(saved2.getId()).getDate()).isEqualTo(date2),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved2.getId()).getTheme().getName()).isEqualTo(
                        "테마2"),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved2.getId()).getMember().getName()).isEqualTo(
                        "헤일러"),
                () -> assertThat(
                        reservationQueryRepository.getByIdOrThrow(saved2.getId()).getTime().getStartAt()).isEqualTo(
                        LocalTime.of(11, 0))
        );
    }

    @Test
    void 특정_날짜에_특정_테마에_대해_이용_가능한_예약_시간_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(date, theme.getId());

        final List<AvailableReservationTimeResponse> before = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .toList();

        reservationCommandRepository.save(
                Reservation.createForRegister(date, time1, theme, member, BookingState.CONFIRMED));
        reservationCommandRepository.save(
                Reservation.createForRegister(date, time2, theme, member, BookingState.CONFIRMED));

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

        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        reservationCommandRepository.save(
                Reservation.createForRegister(date, time, theme, member, BookingState.CONFIRMED));

        final CreateReservationRequest.ForMember request = new CreateReservationRequest.ForMember(date, time.getId(),
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
        final ReservationTime time = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final CreateReservationRequest.ForMember request = new CreateReservationRequest.ForMember(date, time.getId(),
                theme.getId());
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        reservationCommandRepository.save(
                Reservation.createForRegister(date, time, theme, member, BookingState.CONFIRMED));

        // when & then
        assertThatThrownBy(() -> reservationService.createForMember(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
    }

    @Test
    void 특정_회원의_예약_목록을_조회한다() {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeCommandRepository.save(NOT_SAVED_THEME_2());

        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final Reservation saved1 = reservationCommandRepository.save(
                Reservation.createForRegister(date1, time1, theme1, member, BookingState.WAITING)
        );

        final Reservation saved2 = reservationCommandRepository.save(
                Reservation.createForRegister(date2, time2, theme2, member, BookingState.WAITING)
        );

        // when
        final List<ForMember> founds = reservationService.findReservationsByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(founds).hasSize(2),
                () -> assertThat(founds)
                        .containsExactlyInAnyOrder(
                                new ForMember(saved1.getId(), "테마1", LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                                        "대기"),
                                new ForMember(saved2.getId(), "테마2", LocalDate.now().plusDays(2), LocalTime.of(11, 0),
                                        "대기")
                        )
        );
    }
}
