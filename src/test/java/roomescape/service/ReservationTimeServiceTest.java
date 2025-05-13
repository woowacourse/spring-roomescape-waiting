package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationName;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.time.AvailableReservationTimeResponse;
import roomescape.dto.time.ReservationTimeCreateRequest;
import roomescape.dto.time.ReservationTimeResponse;
import roomescape.exception.NotFoundException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeReservationTimeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTimeServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository(new ArrayList<>());
    private final ReservationRepository reservationRepository = new FakeReservationRepository(new ArrayList<>());
    private final ReservationTimeService reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);

    @Nested
    @DisplayName("예약시간 생성")
    class ReservationTimeCreateTest {

        @DisplayName("ReservationTime을 생성할 수 있다")
        @Test
        void createReservationTimeTest() {
            ReservationTimeCreateRequest requestDto = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
            ReservationTimeResponse responseDto = reservationTimeService.createReservationTime(requestDto);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.startAt()).isEqualTo(LocalTime.of(10, 0));
        }

        @DisplayName("이미 예약시간이 존재하면 ReservationTime을 생성할 수 없다")
        @Test
        void createInvalidReservationTimeTest() {
            ReservationTimeCreateRequest requestDto = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
            ReservationTimeCreateRequest invalidRequestDto = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
            reservationTimeService.createReservationTime(requestDto);

            assertThatThrownBy(() -> reservationTimeService.createReservationTime(invalidRequestDto));
        }
    }


    @Nested
    @DisplayName("예약시간 조회")
    class ReservationTimeFindTest {

        @DisplayName("모든 ReservationTime을 조회할 수 있다")
        @Test
        void findAllReservationTimesTest() {
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

            List<ReservationTimeResponse> allTimes = reservationTimeService.findAllReservationTimes();

            assertThat(allTimes).hasSize(2);
            assertThat(allTimes).extracting("startAt").containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
        }

        @DisplayName("특정한 날짜의 이용 가능한 예약시간을 조회한다")
        @Test
        void findAvailableReservationTimesTest() {
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

            ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
            Theme theme = new Theme(1L, "ABC", "DEF", "https://");
            Reservation reservation = new Reservation(1L, new ReservationName(1L, "가이온"), LocalDate.now().plusDays(1), reservationTime, theme);
            reservationRepository.save(reservation);

            List<AvailableReservationTimeResponse> availableReservationTimes = reservationTimeService.findAvailableReservationTimes(LocalDate.now().plusDays(1), 1L);

            boolean alreadyBooked = availableReservationTimes.stream()
                    .filter(dto -> dto.startAt().equals(LocalTime.of(10, 0)))
                    .findFirst().get().alreadyBooked();

            boolean notBooked = availableReservationTimes.stream()
                    .filter(dto -> dto.startAt().equals(LocalTime.of(11, 0)))
                    .findFirst().get().alreadyBooked();

            assertAll(
                    () -> assertThat(availableReservationTimes).hasSize(2),
                    () -> assertThat(alreadyBooked).isTrue(),
                    () -> assertThat(notBooked).isFalse()
            );
        }
    }


    @Nested
    @DisplayName("예약시간 삭제")
    class ReservationTimeDeleteTest {

        @DisplayName("ReservationTime을 삭제할 수 있다")
        @Test
        void deleteReservationTimeByIdTest() {
            ReservationTimeCreateRequest requestDto = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
            ReservationTimeResponse responseDto = reservationTimeService.createReservationTime(requestDto);

            reservationTimeService.deleteReservationTimeById(responseDto.id());

            List<ReservationTimeResponse> allTimes = reservationTimeService.findAllReservationTimes();
            assertThat(allTimes).isEmpty();
        }

        @DisplayName("존재하지 않는 ReservationTime을 삭제하려고 하면 예외가 발생한다")
        @Test
        void deleteNonExistentReservationTimeTest() {
            Long nonExistentId = 2L;

            assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(nonExistentId))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
