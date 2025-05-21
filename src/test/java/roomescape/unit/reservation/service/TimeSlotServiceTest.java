package roomescape.unit.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.exception.ExistedReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.TimeWithBookedResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.service.TimeSlotService;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeThemeRepository;
import roomescape.unit.fake.FakeTimeSlotRepository;

class TimeSlotServiceTest {

    private TimeSlotRepository timeSlotRepository;
    private ReservationRepository reservationRepository;
    private ThemeRepository themeRepository;
    private TimeSlotService timeSlotService;

    private final Theme theme = new Theme(1L, "themeName1", "des", "th");

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();
        timeSlotService = new TimeSlotService(timeSlotRepository, reservationRepository,
                themeRepository);
    }

    @Test
    void 예약_시간을_조회할_수_있다() {
        // given
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        TimeSlot timeSlot2 = new TimeSlot(2L, LocalTime.of(11, 0));
        timeSlotRepository.save(timeSlot1);
        timeSlotRepository.save(timeSlot2);
        // when
        List<ReservationTimeResponse> allTimes = timeSlotService.findAllTimes();

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
        timeSlotService.createTime(newTime);
        List<ReservationTimeResponse> all = timeSlotService.findAllTimes();

        // then
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getLast().startAt()).isEqualTo(LocalTime.of(2, 0));
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        TimeSlot timeSlot2 = new TimeSlot(2L, LocalTime.of(11, 0));
        timeSlotRepository.save(timeSlot1);
        timeSlotRepository.save(timeSlot2);
        // when
        timeSlotService.deleteTimeById(2L);
        List<ReservationTimeResponse> all = timeSlotService.findAllTimes();

        // then
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getFirst().startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 특정_시간에_대한_예약이_존재하면_예약시간을_삭제할_수_없다() {
        // given
        TimeSlot timeSlot1 = TimeSlot.createWithoutId(LocalTime.of(10, 0));
        TimeSlot savedTimeSlot1 = timeSlotRepository.save(timeSlot1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        Reservation reservation1 = Reservation.createWithoutId(
                member1, LocalDate.of(2025, 7, 25), savedTimeSlot1, theme
        );
        reservationRepository.save(reservation1);

        // when & then
        assertThatThrownBy(() -> timeSlotService.deleteTimeById(1L))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 특정날짜와_테마의_예약시간들을_예약여부와_함께_조회한다() {
        // given
        TimeSlot savedTime = timeSlotRepository.save(
                TimeSlot.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("name", "desc", "thumb"));
        Member member = new Member(1L, "name1", "email@domain.com", "pass1", Role.MEMBER);
        reservationRepository.save(
                Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), savedTime, theme)
        );
        // when
        List<TimeWithBookedResponse> filteredTimes = timeSlotService.findTimesByDateAndThemeIdWithBooked(
                LocalDate.of(2025, 1, 1), 1L);
        // then
        assertThat(filteredTimes).hasSize(1);
        assertThat(filteredTimes.getFirst().alreadyBooked()).isTrue();
    }
}
