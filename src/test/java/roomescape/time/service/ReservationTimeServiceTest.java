package roomescape.time.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.Fixtures;
import roomescape.exception.BadRequestException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.ReservationTimeCreateRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("예약 시간 서비스")
class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    private Long id;
    private LocalTime startAt;
    private ReservationTime reservationTimeFixture;

    @BeforeEach
    void setUp() {
        this.reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
        this.id = 1L;
        this.startAt = LocalTime.of(10, 10);
        this.reservationTimeFixture = new ReservationTime(id, startAt);
    }

    @DisplayName("에약 시간 서비스는 시간을 생성한다.")
    @Test
    void createTime() {
        // given
        Mockito.when(reservationTimeRepository.save(any()))
                .thenReturn(reservationTimeFixture);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt);

        // when
        ReservationTimeResponse reservationTime = reservationTimeService.createTime(request);

        // then
        assertThat(reservationTime.startAt())
                .isEqualTo(startAt);
    }

    @DisplayName("예약 시간 서비스는 중복된 예약 시간 요청이 들어오면 예외가 발생한다.")
    @Test
    void validateIsDuplicated() {
        // given
        Mockito.when(reservationTimeRepository.existsByStartAt(reservationTimeFixture.getStartAt()))
                .thenReturn(Boolean.TRUE);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(reservationTimeFixture.getStartAt());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 예약 시간입니다.");
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간을 반환한다.")
    @Test
    void readReservationTime() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(reservationTimeFixture));

        // when
        ReservationTimeResponse reservationTime = reservationTimeService.readReservationTime(id);

        // then
        assertThat(reservationTime.startAt())
                .isEqualTo(startAt);
    }

    @DisplayName("예약 시간 서비스는 시간들을 반환한다.")
    @Test
    void readReservationTimes() {
        // given
        Mockito.when(reservationTimeRepository.findAll())
                .thenReturn(List.of(reservationTimeFixture));

        // when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.readReservationTimes(null, null);

        // then
        assertThat(reservationTimes.size()).isEqualTo(1);
    }

    @DisplayName("예약 시간 서비스는 지정된 날짜와 테마별 예약 가능 여부를 포함하여 시간들을 반환한다.")
    @Test
    void readReservationTimesByDateAndThemeId() {
        // given
        LocalDate date = LocalDate.of(2024, 12, 2);
        Long themeId = 2L;
        Mockito.when(reservationRepository.findByDateAndThemeId(date, themeId))
                .thenReturn(List.of(Fixtures.reservationFixture));
        Mockito.when(reservationTimeRepository.findAll())
                .thenReturn(List.of(reservationTimeFixture));

        // when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.readReservationTimes(date, themeId);

        // then
        assertThat(reservationTimes).hasSize(1);
        assertThat(reservationTimes).contains(ReservationTimeResponse.of(reservationTimeFixture, true));
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간을 삭제한다.")
    @Test
    void deleteTime() {
        // given
        Mockito.when(reservationRepository.existsByTimeId(id))
                .thenReturn(false);
        Mockito.when(reservationTimeRepository.deleteById(id))
                .thenReturn(1);

        // when & then
        assertThatCode(() -> reservationTimeService.deleteTime(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간에 예약이 존재하면 예외가 발생한다.")
    @Test
    void deleteTimeWithExistsReservation() {
        // given
        Mockito.when(reservationRepository.existsByTimeId(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(id))
                .isInstanceOf(BadRequestException.class);
    }
}
