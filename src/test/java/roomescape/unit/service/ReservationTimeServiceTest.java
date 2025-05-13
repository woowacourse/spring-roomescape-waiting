package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.service.ReservationTimeService;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private ReservationTimeService reservationTimeService;

    private final Theme theme = new Theme(1L, "themeName1", "des", "th");

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository(reservationRepository);
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 예약_시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        // when
        List<ReservationTimeResponse> allTimes = reservationTimeService.findAllTimes();

        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allTimes.size()).isEqualTo(2);
        soft.assertThat(allTimes.get(0).startAt()).isEqualTo(LocalTime.of(10, 0));
        soft.assertThat(allTimes.get(1).startAt()).isEqualTo(LocalTime.of(11, 0));
        soft.assertAll();
    }

    @Test
    void 예약_시간을_추가할_수_있다() {
        // given & when
        ReservationTimeRequest newTime = new ReservationTimeRequest(LocalTime.of(2, 0));
        reservationTimeService.createTime(newTime);
        List<ReservationTimeResponse> all = reservationTimeService.findAllTimes();

        // then
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getLast().startAt()).isEqualTo(LocalTime.of(2, 0));
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        // when
        reservationTimeService.deleteTimeById(2L);
        List<ReservationTimeResponse> all = reservationTimeService.findAllTimes();

        // then
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getFirst().startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 특정_시간에_대한_예약이_존재하면_예약시간을_삭제할_수_없다() {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime1 = reservationTimeRepository.save(reservationTime1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        Reservation reservation1 = Reservation.createWithoutId(
                member1, LocalDate.of(2025, 7, 25), savedReservationTime1, theme
        );
        reservationRepository.save(reservation1);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTimeById(1L))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 특정날짜와_테마의_예약시간들을_예약여부와_함께_조회한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = new Theme(1L, "name", "desc", "thumb");
        Member member = new Member(1L, "name1", "email@domain.com", "pass1", Role.MEMBER);
        reservationRepository.save(
                Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), savedTime, theme)
        );
        // when
        List<AvailableTimeResponse> filteredTimes = reservationTimeService.findTimesByDateAndThemeIdWithBooked(
                LocalDate.of(2025, 1, 1), 1L);
        // then
        assertThat(filteredTimes).hasSize(1);
        assertThat(filteredTimes.getFirst().alreadyBooked()).isTrue();
    }
}
