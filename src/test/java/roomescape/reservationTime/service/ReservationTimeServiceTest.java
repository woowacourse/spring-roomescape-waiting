package roomescape.reservationTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.dto.request.TimeConditionRequest;
import roomescape.reservationTime.dto.response.TimeConditionResponse;
import roomescape.reservationTime.infrastructure.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationTimeServiceTest {
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void beforeEach() {
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(11, 0));
        Theme theme = Theme.createWithId(1L, "테마", "테마", "테마");

        List<ReservationTime> reservationTimes = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();

        ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository(reservationTimes);
        ReservationTime save = reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);

        reservationTime1 = reservationTimeRepository.findById(save.getId()).orElseThrow();
        ReservationRepository reservationRepository = new FakeReservationRepository(reservations);
        Member member = Member.createWithId(1L, "홍길동", "a@com", "a", Role.USER);
        reservationRepository.save(Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2024, 10, 6), reservationTime1, theme));

        reservationTimeService = new ReservationTimeService(reservationRepository, reservationTimeRepository);
    }

    @DisplayName("이미 존재하는 예약이 있는 경우 예약 시간을 삭제할 수 없다.")
    @Test
    void can_not_delete_when_reservation_exists() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 가능 시간 조회 테스트")
    @Test
    void time_condition_test() {
        // given
        LocalDate localDate = LocalDate.of(2024, 10, 6);
        Long themeId = 1L;
        // when
        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(
                new TimeConditionRequest(localDate, themeId));
        // then
        assertThat(responses).containsExactlyInAnyOrder(
                new TimeConditionResponse(1L, LocalTime.of(10, 0), true),
                new TimeConditionResponse(2L, LocalTime.of(11, 0), false)
        );
    }
}
