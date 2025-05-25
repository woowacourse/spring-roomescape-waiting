package roomescape.unit.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.timeslot.dto.TimeSlotAvailabilityInfo;
import roomescape.reservation.application.timeslot.dto.TimeSlotCreateCommand;
import roomescape.reservation.application.timeslot.dto.TimeSlotInfo;
import roomescape.reservation.application.timeslot.service.TimeSlotService;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeTimeSlotRepository;

class TimeSlotServiceTest {

    private final TimeSlotRepository timeSlotRepository = new FakeTimeSlotRepository();
    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final TimeSlotService timeSlotService = new TimeSlotService(timeSlotRepository,
            reservationRepository);

    @DisplayName("이미 존재하는 시간을 저장할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCreateDuplicateTime() {
        // given
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(LocalTime.of(11, 0));
        timeSlotService.createTimeSlot(request);
        // when
        // then
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 저장할 수 있다")
    @Test
    void create() {
        // given
        final LocalTime time = LocalTime.of(11, 0);
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(time);
        // when
        final TimeSlotInfo result = timeSlotService.createTimeSlot(request);
        // then
        final TimeSlot savedTime = timeSlotRepository.findById(1L).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.id()).isEqualTo(1L),
                () -> assertThat(savedTime.startAt()).isEqualTo(time)
        );
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        final TimeSlotCreateCommand request1 = new TimeSlotCreateCommand(LocalTime.of(11, 0));
        final TimeSlotCreateCommand request2 = new TimeSlotCreateCommand(LocalTime.of(12, 0));
        timeSlotService.createTimeSlot(request1);
        timeSlotService.createTimeSlot(request2);
        // when
        final List<TimeSlotInfo> result = timeSlotService.findTimeSlots();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 시간을 삭제할 수 있다")
    @Test
    void delete() {
        // given
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(LocalTime.of(11, 0));
        timeSlotService.createTimeSlot(request);
        // when
        timeSlotService.deleteTimeSlotById(1L);
        // then
        assertThat(timeSlotRepository.findById(1L)).isEmpty();
    }

    @DisplayName("예약이 존재하는 시간은 삭제할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteTimeWithinReservation() {
        // given
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(LocalTime.of(11, 0));
        final TimeSlotInfo response = timeSlotService.createTimeSlot(request);
        final TimeSlot time = new TimeSlot(response.id(), response.startAt());
        final Theme theme = new Theme(1L, "테마", "설명", "썸네일.png");
        final Member member = new Member("리버", "river@email.com", "riverpw", MemberRole.ADMIN);
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), member, time, theme));
        // when
        // then
        assertThatThrownBy(() -> timeSlotService.deleteTimeSlotById(response.id()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("예약 가능 시간을 조회할 수 있다")
    @Test
    void findAvailableTimes() {
        // given
        final TimeSlot savedTime1 = timeSlotRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        final TimeSlot savedTime2 = timeSlotRepository.save(new TimeSlot(LocalTime.of(15, 0)));
        final Theme theme = new Theme(1L, "테마", "설명", "썸네일.png");
        final Member member = new Member("리버", "river@email.com", "riverpw", MemberRole.ADMIN);
        final LocalDate date = LocalDate.of(2025, 5, 1);
        reservationRepository.save(new Reservation(1L, date, member, savedTime1, theme));
        // when
        final List<TimeSlotAvailabilityInfo> result = timeSlotService.findAvailableTimeSlots(date, theme.id());
        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).contains(
                        new TimeSlotAvailabilityInfo(savedTime1.id(), savedTime1.startAt(), true),
                        new TimeSlotAvailabilityInfo(savedTime2.id(), savedTime2.startAt(), false)
                )
        );
    }
}
