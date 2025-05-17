package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_2;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_2;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
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
import roomescape.member.domain.MemberCommandRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationStatus;
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

    @Test
    void 예약을_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Long timeId = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1()).getId();
        final Long themeId = themeCommandRepository.save(NOT_SAVED_THEME_1()).getId();
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());
        final CreateReservationRequest.ForMember request = new CreateReservationRequest.ForMember(date, timeId,
                themeId);
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        Assertions.assertThatCode(() -> reservationService.create(request, memberAuthInfo.id()))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약을_추가할_때_이미_예약된_시간이면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        reservationCommandRepository.save(
                new Reservation(date, reservationTime, theme, member, ReservationStatus.CONFIRMED));

        final CreateReservationRequest.ForMember request =
                new CreateReservationRequest.ForMember(date, reservationTime.getId(), theme.getId());
        final MemberAuthInfo memberAuthInfo =
                new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.create(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 과거_시간으로_예약을_추가하면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().minusDays(5);
        final Long timeId = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1()).getId();
        final Long themeId = themeCommandRepository.save(NOT_SAVED_THEME_1()).getId();
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final CreateReservationRequest.ForMember request =
                new CreateReservationRequest.ForMember(date, timeId, themeId);
        final MemberAuthInfo memberAuthInfo =
                new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.create(request, memberAuthInfo.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 한_테마의_날짜와_시간이_중복되는_예약을_추가할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final CreateReservationRequest.ForMember request =
                new CreateReservationRequest.ForMember(date, reservationTime.getId(), theme.getId());
        final MemberAuthInfo memberAuthInfo =
                new MemberAuthInfo(member.getId(), member.getRole());

        reservationCommandRepository.save(
                new Reservation(date, reservationTime, theme, member, ReservationStatus.CONFIRMED));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.create(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member1 = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final Reservation reservation = new Reservation(date, reservationTime1, theme1, member1,
                ReservationStatus.CONFIRMED);
        final Long reservationId = reservationCommandRepository.save(reservation).getId();
        final MemberAuthInfo member1AuthInfo = new MemberAuthInfo(member1.getId(), member1.getRole());

        // when & then
        Assertions.assertThatCode(() -> reservationService.deleteIfOwner(reservationId, member1AuthInfo))
                .doesNotThrowAnyException();
    }

    @Test
    void 본인의_예약이_아니면_삭제할_수_없다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());

        final Member member1 = memberCommandRepository.save(NOT_SAVED_MEMBER_1());
        final Member member2 = memberCommandRepository.save(NOT_SAVED_MEMBER_2());

        final Reservation reservation = new Reservation(date, reservationTime1, theme1, member1,
                ReservationStatus.CONFIRMED);
        final Long reservationId = reservationCommandRepository.save(reservation).getId();
        final MemberAuthInfo member2AuthInfo = new MemberAuthInfo(member2.getId(), member2.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteIfOwner(reservationId, member2AuthInfo))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void 예약_목록_전체를_조회한다() {
        // given
        final int beforeCount = reservationService.findAll().size();

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeCommandRepository.save(NOT_SAVED_THEME_2());

        reservationCommandRepository.save(
                new Reservation(date1, time1, theme1, member, ReservationStatus.CONFIRMED)
        );
        reservationCommandRepository.save(
                new Reservation(date2, time2, theme2, member, ReservationStatus.CONFIRMED)
        );

        // when
        final int afterCount = reservationService.findAll().size();

        // then
        assertThat(afterCount - beforeCount).isEqualTo(2);
    }

    @Test
    void 특정_날짜에_특정_테마에_대해_이용_가능한_예약_시간_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final ReservationTime reservationTime1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());

        final AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(date, theme.getId());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final long beforeCount = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .count();
        reservationCommandRepository.save(
                new Reservation(date, reservationTime1, theme, member, ReservationStatus.CONFIRMED));

        // when
        final long afterCount = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .count();

        // then
        assertThat(afterCount - beforeCount).isEqualTo(1);
    }

    @Test
    void 특정_회원의_예약_목록을_조회한다() {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());
        final Theme theme1 = themeCommandRepository.save(NOT_SAVED_THEME_1());
        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_2());
        final Theme theme2 = themeCommandRepository.save(NOT_SAVED_THEME_2());

        reservationCommandRepository.save(
                new Reservation(date1, time1, theme1, member, ReservationStatus.CONFIRMED)
        );

        reservationCommandRepository.save(
                new Reservation(date2, time2, theme2, member, ReservationStatus.CONFIRMED)
        );

        // when
        final List<ForMember> founds = reservationService.findReservationsByMemberId(member.getId());

        // then
        assertThat(founds).hasSize(2);
    }
}
