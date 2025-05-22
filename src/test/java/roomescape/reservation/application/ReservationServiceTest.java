package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.MemberFixture.notSavedMember2;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime2;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme2;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse.ForMember;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@DisplayName("회원 예약 관리 서비스")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 회원_권한으로_예약을_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Long timeId = reservationTimeRepository.save(notSavedReservationTime1()).getId();
        final Long themeId = themeRepository.save(notSavedTheme1()).getId();
        final Member member = memberRepository.save(notSavedMember1());
        final CreateReservationRequest.ForMember request = new CreateReservationRequest.ForMember(date, timeId,
                themeId);
        final MemberAuthInfo memberAuthInfo = new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        Assertions.assertThatCode(() -> reservationService.create(request, memberAuthInfo.id()))
                .doesNotThrowAnyException();
    }

    @Test
    void 회원_권한으로_과거_시간에_예약을_추가하려_하면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().minusDays(5);
        final Long timeId = reservationTimeRepository.save(notSavedReservationTime1()).getId();
        final Long themeId = themeRepository.save(notSavedTheme1()).getId();
        final Member member = memberRepository.save(notSavedMember1());

        final CreateReservationRequest.ForMember request =
                new CreateReservationRequest.ForMember(date, timeId, themeId);
        final MemberAuthInfo memberAuthInfo =
                new MemberAuthInfo(member.getId(), member.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.create(request, memberAuthInfo.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_예약된_테마_날짜_시간으로_예약_추가를_시도하면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        final CreateReservationRequest.ForMember request =
                new CreateReservationRequest.ForMember(date, reservationTime.getId(), theme.getId());
        final MemberAuthInfo memberAuthInfo =
                new MemberAuthInfo(member.getId(), member.getRole());

        reservationRepository.save(
                new Reservation(date, reservationTime, theme, member, ReservationStatus.CONFIRMED));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.create(request, memberAuthInfo.id()))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 회원이_본인의_예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime1 = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme1 = themeRepository.save(notSavedTheme1());
        final Member member1 = memberRepository.save(notSavedMember1());

        final Reservation reservation = new Reservation(date, reservationTime1, theme1, member1,
                ReservationStatus.CONFIRMED);
        final Long reservationId = reservationRepository.save(reservation).getId();
        final MemberAuthInfo member1AuthInfo = new MemberAuthInfo(member1.getId(), member1.getRole());

        // when & then
        Assertions.assertThatCode(() -> reservationService.deleteIfOwner(reservationId, member1AuthInfo.id()))
                .doesNotThrowAnyException();
    }

    @Test
    void 회원이_본인의_예약이_아닌_예약을_삭제하려_하면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime1 = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme1 = themeRepository.save(notSavedTheme1());

        final Member member1 = memberRepository.save(notSavedMember1());
        final Member member2 = memberRepository.save(notSavedMember2());

        final Reservation reservation = new Reservation(date, reservationTime1, theme1, member1,
                ReservationStatus.CONFIRMED);
        final Long reservationId = reservationRepository.save(reservation).getId();
        final MemberAuthInfo member2AuthInfo = new MemberAuthInfo(member2.getId(), member2.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteIfOwner(reservationId, member2AuthInfo.id()))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void 삭제하려는_예약이_존재하지_않는_경우_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime1 = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme1 = themeRepository.save(notSavedTheme1());
        final Member member1 = memberRepository.save(notSavedMember1());

        final Reservation reservation = new Reservation(date, reservationTime1, theme1, member1,
                ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
        final MemberAuthInfo member1AuthInfo = new MemberAuthInfo(member1.getId(), member1.getRole());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteIfOwner(Long.MAX_VALUE, member1AuthInfo.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 특정_날짜에_특정_테마에_대해_이용_가능한_예약_시간_목록을_조회한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationTime reservationTime1 = reservationTimeRepository.save(notSavedReservationTime1());
        reservationTimeRepository.save(notSavedReservationTime2());

        final AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(date, theme.getId());
        final Member member = memberRepository.save(notSavedMember1());

        final long beforeCount = reservationService.findAvailableReservationTimes(request)
                .stream()
                .filter(AvailableReservationTimeResponse::alreadyBooked)
                .count();
        reservationRepository.save(
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
    void 회원_본인의_예약_목록을_조회한다() {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme1 = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeRepository.save(notSavedReservationTime2());
        final Theme theme2 = themeRepository.save(notSavedTheme2());

        reservationRepository.save(
                new Reservation(date1, time1, theme1, member, ReservationStatus.CONFIRMED)
        );

        reservationRepository.save(
                new Reservation(date2, time2, theme2, member, ReservationStatus.CONFIRMED)
        );

        // when
        final List<ForMember> founds = reservationService.findReservationsByMemberId(member.getId());

        // then
        assertThat(founds).hasSize(2);
    }
}
