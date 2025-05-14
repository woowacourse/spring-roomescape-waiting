package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.exception.NotFoundException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeReservationTimeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTimeServiceTest {

    ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository(new ArrayList<>());
    ReservationRepository reservationRepository = new FakeReservationRepository(new ArrayList<>());
    ReservationTimeService reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);

    @Nested
    @DisplayName("예약시간 생성")
    class ReservationTimeCreateTest {

        @DisplayName("ReservationTime을 생성할 수 있다")
        @Test
        void createReservationTimeTest() {
            ReservationTimeCreateRequestDto requestDto = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));
            ReservationTimeResponseDto responseDto = reservationTimeService.createReservationTime(requestDto);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.startAt()).isEqualTo(LocalTime.of(10, 0));
        }

        @DisplayName("이미 예약시간이 존재하면 ReservationTime을 생성할 수 없다")
        @Test
        void createInvalidReservationTimeTest() {
            ReservationTimeCreateRequestDto requestDto = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));
            ReservationTimeCreateRequestDto invalidRequestDto = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));
            reservationTimeService.createReservationTime(requestDto);

            Assertions.assertThatThrownBy(() -> reservationTimeService.createReservationTime(invalidRequestDto));
        }
    }


    @Nested
    @DisplayName("예약시간 조회")
    class ReservationTimeFindTest {

        @DisplayName("모든 ReservationTime을 조회할 수 있다")
        @Test
        void findAllReservationTimesTest() {
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequestDto(LocalTime.of(10, 0)));
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequestDto(LocalTime.of(11, 0)));

            List<ReservationTimeResponseDto> allTimes = reservationTimeService.findAllReservationTimes();

            assertThat(allTimes).hasSize(2);
            assertThat(allTimes).extracting("startAt").containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
        }

        @DisplayName("특정한 날짜의 이용 가능한 예약시간을 조회한다")
        @Test
        void findAvailableReservationTimesTest() {
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequestDto(LocalTime.of(10, 0)));
            reservationTimeService.createReservationTime(new ReservationTimeCreateRequestDto(LocalTime.of(11, 0)));
            Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");

            ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
            Theme theme = new Theme(1L, "ABC", "DEF", "GHI");
            Reservation reservation = new Reservation(1L, member, LocalDate.now().plusDays(1), reservationTime, theme);
            reservationRepository.save(reservation);

            List<AvailableReservationTimeResponseDto> availableReservationTimes = reservationTimeService.findAvailableReservationTimes(LocalDate.now().plusDays(1), 1L);

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
            ReservationTimeCreateRequestDto requestDto = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));
            ReservationTimeResponseDto responseDto = reservationTimeService.createReservationTime(requestDto);

            reservationTimeService.deleteReservationTimeById(responseDto.id());

            List<ReservationTimeResponseDto> allTimes = reservationTimeService.findAllReservationTimes();
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
