package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.domain.MemberFixture.notSavedMember1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime1;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime2;
import static roomescape.fixture.domain.ReservationTimeFixture.notSavedReservationTime3;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme1;
import static roomescape.fixture.domain.ThemeFixture.notSavedTheme2;
import static roomescape.reservation.domain.ReservationStatus.BOOKED;
import static roomescape.reservation.domain.ReservationStatus.values;

import java.time.LocalDate;
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
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.FilteredReservationsRequest;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;
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
                new CreateReservationRequest(member.getId(), date, timeId, themeId, BOOKED);

        // when
        final ReservationResponse response = adminReservationService.create(request);

        // then
        SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(response.date()).isEqualTo(date);
                    softly.assertThat(response.time().id()).isEqualTo(timeId);
                    softly.assertThat(response.theme().id()).isEqualTo(themeId);
                    softly.assertThat(response.member().id()).isEqualTo(member.getId());
                    softly.assertThat(response.status()).isEqualTo(BOOKED.getDescription());
                }
        );
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
                        BOOKED);

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
                Reservation.of(ReservationSlot.of(date, reservationTime, theme), member, BOOKED));

        final CreateReservationRequest request =
                new CreateReservationRequest(member.getId(), date, reservationTime.getId(), theme.getId()
                        , BOOKED);

        // when & then
        Assertions.assertThatThrownBy(() -> adminReservationService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final ReservationTime time = reservationTimeRepository.save(notSavedReservationTime1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final Member member = memberRepository.save(notSavedMember1());
        final Reservation reservation = reservationRepository.save(
                Reservation.of(ReservationSlot.of(date, time, theme), member, BOOKED));

        // when
        adminReservationService.deleteAsAdmin(reservation.getId());

        // then
        Assertions.assertThat(reservationRepository.findById(reservation.getId()).isEmpty()).isTrue();
    }

    @Test
    void 삭제하려는_예약이_존재하지_않는_경우_예외가_발생한다() {
        // given
        final Long notExistWaitingId = Long.MAX_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> adminReservationService.deleteAsAdmin(notExistWaitingId))
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
                Reservation.of(ReservationSlot.of(date1, time1, theme1), member, BOOKED)
        );
        reservationRepository.save(
                Reservation.of(ReservationSlot.of(date2, time2, theme2), member, BOOKED)
        );

        // when
        final int afterCount = adminReservationService.findAll().size();

        // then
        assertThat(afterCount - beforeCount).isEqualTo(2);
    }

    @Test
    void 테마_회원_날짜_간격을_기준으로_예약을_조회한다() {
        // given
        final Member member = memberRepository.save(notSavedMember1());
        final Theme theme = themeRepository.save(notSavedTheme1());
        final ReservationTime time1 = reservationTimeRepository.save(notSavedReservationTime1());
        final ReservationTime time2 = reservationTimeRepository.save(notSavedReservationTime2());
        final ReservationTime time3 = reservationTimeRepository.save(notSavedReservationTime3());

        final LocalDate date1 = LocalDate.now().plusDays(1);
        final LocalDate date2 = LocalDate.now().plusDays(2);
        final LocalDate date3 = LocalDate.now().plusDays(3);

        reservationRepository.save(Reservation.of(ReservationSlot.of(date1, time1, theme), member, BOOKED));
        reservationRepository.save(Reservation.of(ReservationSlot.of(date2, time2, theme), member, BOOKED));
        reservationRepository.save(Reservation.of(ReservationSlot.of(date3, time3, theme), member, BOOKED));

        final FilteredReservationsRequest request1 =
                new FilteredReservationsRequest(theme.getId(), member.getId(), date1, date2);
        final FilteredReservationsRequest request2 =
                new FilteredReservationsRequest(theme.getId(), member.getId(), date1, date3);
        final FilteredReservationsRequest request3 =
                new FilteredReservationsRequest(theme.getId(), member.getId(), date3, date3.plusDays(1));
        final FilteredReservationsRequest request4 =
                new FilteredReservationsRequest(theme.getId(), member.getId(), date3.plusDays(1), date3.plusDays(2));

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

    @Test
    void 예약_상태_목록을_조회한다() {
        // when
        final List<ReservationStatusResponse> responses = adminReservationService.findAllReservationStatuses();

        // then
        assertThat(responses).hasSize(values().length);
    }
}
