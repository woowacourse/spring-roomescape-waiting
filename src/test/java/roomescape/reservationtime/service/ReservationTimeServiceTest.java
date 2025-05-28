package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.exception.BadRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@SpringBootTest
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 예약_시간을_생성할_수_있다() {
        // given
        ReservationTime reservationTimeToSave = ReservationTimeFixture.createWithoutId();
        ReservationTime reservationTimeAfterSave = ReservationTime.createWithPrimaryKey(
            reservationTimeToSave,
            1L
        );
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(reservationTimeToSave.getStartAt());
        ReservationTimeResponse extected = new ReservationTimeResponse(
            reservationTimeAfterSave.getId(),
            reservationTimeToSave.getStartAt()
        );
        when(reservationTimeRepository.save(reservationTimeToSave))
            .thenReturn(reservationTimeAfterSave);

        // when
        ReservationTimeResponse actual = reservationTimeService.create(request);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(extected);
    }

    @Test
    void 모든_예약_시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime1 = ReservationTimeFixture.create();
        ReservationTime reservationTime2 = ReservationTimeFixture.create();
        List<ReservationTimeResponse> expected = List.of(
            ReservationTimeResponse.fromReservationTime(reservationTime1),
            ReservationTimeResponse.fromReservationTime(reservationTime2)
        );
        when(reservationTimeRepository.findAll())
            .thenReturn(List.of(reservationTime1, reservationTime2));

        // when
        List<ReservationTimeResponse> actual = reservationTimeService.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void id_를_통해_예약_시간을_삭제할_수_있다() {
        // given
        Long idToDelete = 1L;

        // when
        reservationTimeService.deleteById(idToDelete);

        // then
        verify(reservationTimeRepository).deleteById(idToDelete);
    }

    @Test
    void 날짜와_테마_id_를_기준으로_모든_예약_시간을_예약_가능_여부와_함꼐_조회할_수_있다() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        List<ReservationTime> notBookedReservationTimes = IntStream.rangeClosed(0, 4)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();
        List<ReservationTime> bookedReservationTimes = IntStream.rangeClosed(0, 3)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();

        List<ReservationTime> allTimes = Stream.concat(
            notBookedReservationTimes.stream(),
            bookedReservationTimes.stream()
        ).toList();

        when(reservationTimeRepository.findAll())
            .thenReturn(allTimes);
        when(reservationTimeRepository.findAllByDateAndThemeId(
            date, themeId
        )).thenReturn(notBookedReservationTimes);

        List<ReservationTimeResponseWithBookedStatus> expected = allTimes.stream()
            .map(time ->
                new ReservationTimeResponseWithBookedStatus(
                    time.getId(),
                    time.getStartAt(),
                    !notBookedReservationTimes.contains(time)
                )
            ).toList();

        // when
        List<ReservationTimeResponseWithBookedStatus> actual =
            reservationTimeService.findAvailableReservationTimesByDateAndThemeId(date, themeId);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 예약_시간_id로_예약_시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime = ReservationTimeFixture.create();
        when(reservationTimeRepository.findById(reservationTime.getId()))
            .thenReturn(java.util.Optional.of(reservationTime));

        // when
        ReservationTime actual = reservationTimeService.findByIdOrThrow(reservationTime.getId());

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(reservationTime);
    }

    @Test
    void 존재하지_않는_예약_시간_id로_조회할_경우_예외가_발생한다() {
        // given
        Long nonExistentId = 1L;
        when(reservationTimeRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.findByIdOrThrow(nonExistentId))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 날짜와_테마_id_를_기준으로_예약_가능한_예약_시간을_조회할_수_있다() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        List<ReservationTime> expected = IntStream.rangeClosed(0, 4)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();

        when(reservationTimeRepository.findAllByDateAndThemeId(date, themeId))
            .thenReturn(expected);

        // when
        List<ReservationTime> actual =
            reservationTimeService.findByReservationDateAndThemeId(date, themeId);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
