package roomescape.unit.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.ReservationTimeService;
import roomescape.service.request.AvailableReservationTimeRequest;
import roomescape.service.request.CreateReservationTimeRequest;
import roomescape.service.response.AvailableReservationTimeResponse;
import roomescape.service.response.ReservationTimeResponse;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final ReservationRepository reservationRepository = new FakeReservationRepository();

    private final ReservationTimeService service = new ReservationTimeService(
            reservationTimeRepository,
            reservationRepository
    );

    @Test
    void 예약시간을_생성할_수_있다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(startAt);

        // when
        ReservationTimeResponse response = service.createReservationTime(request);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.id()).isEqualTo(1L);
        softly.assertThat(response.startAt()).isEqualTo(startAt);
        softly.assertAll();
    }

    @Test
    void 중복된_예약시간은_생성할_수_없다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        reservationTimeRepository.save(startAt);
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(startAt);

        // when & then
        assertThatThrownBy(() -> service.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 모든_예약시간을_조회할_수_있다() {
        // given
        reservationTimeRepository.save(LocalTime.of(10, 0));
        reservationTimeRepository.save(LocalTime.of(11, 0));

        // when
        List<ReservationTimeResponse> result = service.findAllReservationTimes();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 예약이_없는_시간은_삭제할_수_있다() {
        // given
        ReservationTime time = reservationTimeRepository.save(LocalTime.of(10, 0));

        // when
        service.deleteReservationTimeById(time.getId());

        // then
        assertThat(reservationTimeRepository.findById(time.getId())).isNotPresent();
    }

    @Test
    void 예약이_존재하는_시간은_삭제할_수_없다() {
        // given
        ReservationTime time = reservationTimeRepository.save(LocalTime.of(10, 0));
        Reservation reservation = reservationRepository.save(
                new Member(
                        1L,
                        new MemberName("한스"),
                        new MemberEmail("leehyeonsu4888@gmail.com"),
                        new MemberEncodedPassword("dsa"),
                        MemberRole.MEMBER
                ),
                new ReservationDateTime(
                        new ReservationDate(LocalDate.of(2025, 5, 5)), time, FIXED_CLOCK
                ),
                new Theme(
                        1L,
                        new ThemeName("공포"),
                        new ThemeDescription("공포입니다."),
                        new ThemeThumbnail("썸네일")
                )
        );

        // when & then
        assertThatThrownBy(() -> service.deleteReservationTimeById(time.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_예약시간은_삭제할_수_없다() {
        // when & then
        assertThatThrownBy(() -> service.deleteReservationTimeById(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약시간을_ID로_조회할_수_있다() {
        // given
        ReservationTime time = reservationTimeRepository.save(LocalTime.of(10, 0));

        // when
        ReservationTime found = service.getReservationTime(1L);

        // then
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void 예약시간_ID로_조회시_없으면_예외() {
        // when & then
        assertThatThrownBy(() -> service.getReservationTime(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약가능한_시간들을_조회할_수_있다() {
        // given
        reservationTimeRepository.save(LocalTime.of(10, 0));
        reservationTimeRepository.save(LocalTime.of(11, 0));

        // when
        List<AvailableReservationTimeResponse> result = service.findAvailableReservationTimes(
                new AvailableReservationTimeRequest(LocalDate.of(2025, 5, 5), 1L));

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.get(0).id()).isEqualTo(1L);
            softly.assertThat(result.get(0).startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(result.get(0).isReserved()).isFalse();
        });
    }
}
