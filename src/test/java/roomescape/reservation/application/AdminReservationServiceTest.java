package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime2;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime3;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme2;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.domain.AuthRole;
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
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByFilterRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@DisplayName("관리자 예약 관리 서비스")
class AdminReservationServiceTest {

    @Autowired
    private AdminReservationService adminReservationService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 특정_회원의_예약을_추가한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final Long timeId = reservationTimeRepository.save(notSavedReservationTime1()).getId();
        final Long themeId = themeRepository.save(notSavedTheme1()).getId();
        final Member member = memberRepository.save(notSavedMember1());
        final CreateReservationRequest request =
                new CreateReservationRequest(member.getId(), date, timeId, themeId, ReservationStatus.CONFIRMED);

        // when & then
        Assertions.assertThatCode(() -> adminReservationService.create(request))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약을_추가할_때_이미_예약된_시간이면_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        reservationRepository.save(
                new Reservation(date, reservationTime, theme, member, ReservationStatus.CONFIRMED));

        final CreateReservationRequest request =
                new CreateReservationRequest(member.getId(), date, reservationTime.getId(), theme.getId()
                        , ReservationStatus.CONFIRMED);

        // when & then
        Assertions.assertThatThrownBy(() -> adminReservationService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 관리자는_과거_시간에_예약을_추가할_수_있다() {
        // given
        final LocalDate date = LocalDate.now().minusDays(5);
        final Long timeId = reservationTimeRepository.save(notSavedReservationTime1()).getId();
        final Long themeId = themeRepository.save(notSavedTheme1()).getId();
        final Member member = memberRepository.save(notSavedMember1());

        final CreateReservationRequest request =
                new CreateReservationRequest(member.getId(), date, timeId, themeId,
                        ReservationStatus.CONFIRMED);

        // when & then
        Assertions.assertThatCode(() -> adminReservationService.create(request))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Reservation reservation = reservationRepository.save(
                new Reservation(date, time, theme, member, ReservationStatus.CONFIRMED));

        // when & then
        Assertions.assertThatCode(() -> adminReservationService.deleteAsAdmin(reservation.getId(), ADMIN))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, names = {"ADMIN"})
    void 관리자_권한이_아닌데_예약을_삭제하려_하면_예외가_발생한다(final AuthRole authRole) {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Reservation reservation = reservationRepository.save(
                new Reservation(date, time, theme, member, ReservationStatus.CONFIRMED));

        // when & then
        Assertions.assertThatThrownBy(() -> adminReservationService.deleteAsAdmin(reservation.getId(), authRole))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void 삭제하려는_예약이_존재하지_않는_경우_예외가_발생한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        reservationRepository.save(new Reservation(date, time, theme, member, ReservationStatus.CONFIRMED));

        // when & then
        Assertions.assertThatThrownBy(() -> adminReservationService.deleteAsAdmin(Long.MAX_VALUE, ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약_목록_전체를_조회한다() {
        // given
        final int beforeCount = reservationRepository.findAll().size();

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
        final int afterCount = adminReservationService.findAll().size();

        // then
        assertThat(afterCount - beforeCount).isEqualTo(2);
    }

    @Test
    void 특정_회원의_특정_기간_동안_특정_테마의_예약_목록을_조회한다() {
        // given
        final LocalDate date1 = LocalDate.now().plusDays(1);
        final ReservationTime time1 = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());

        final LocalDate date2 = LocalDate.now().plusDays(2);
        final ReservationTime time2 = reservationTimeRepository.save(notSavedReservationTime2());

        final LocalDate date3 = LocalDate.now().plusDays(3);
        final ReservationTime time3 = reservationTimeRepository.save(notSavedReservationTime3());

        reservationRepository.save(new Reservation(date1, time1, theme, member, ReservationStatus.CONFIRMED));
        reservationRepository.save(new Reservation(date2, time2, theme, member, ReservationStatus.CONFIRMED));
        reservationRepository.save(new Reservation(date3, time3, theme, member, ReservationStatus.CONFIRMED));

        final ReservationsByFilterRequest request1 =
                new ReservationsByFilterRequest(theme.getId(), member.getId(), date1, date2);
        final ReservationsByFilterRequest request2 =
                new ReservationsByFilterRequest(theme.getId(), member.getId(), date1, date3);
        final ReservationsByFilterRequest request3 =
                new ReservationsByFilterRequest(theme.getId(), member.getId(), date3, date3.plusDays(1));
        final ReservationsByFilterRequest request4 =
                new ReservationsByFilterRequest(theme.getId(), member.getId(), date3.plusDays(1), date3.plusDays(1));

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(adminReservationService.findAllByFilter(request1))
                    .hasSize(2);
            softly.assertThat(adminReservationService.findAllByFilter(request2))
                    .hasSize(3);
            softly.assertThat(adminReservationService.findAllByFilter(request3))
                    .hasSize(1);
            softly.assertThat(adminReservationService.findAllByFilter(request4))
                    .hasSize(0);
        });
    }
}
