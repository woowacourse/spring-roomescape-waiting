package roomescape.domain.reservationslot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.NotFoundException;
import roomescape.support.fake.FakeReservationDateRepository;
import roomescape.support.fake.FakeThemeRepository;

class ReservationSlotServiceTest {

    private FakeThemeRepository themeRepository;
    private FakeReservationDateRepository reservationDateRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        reservationDateRepository = new FakeReservationDateRepository();
        reservationRepository = org.mockito.Mockito.mock(ReservationRepository.class);
    }

    @Test
    @DisplayName("예약 슬롯별 현재 예약 인원을 조회한다.")
    void getReservationSlots() {
        // given
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 16))
        );
        given(reservationRepository.countReservation(theme.getId(), reservationDate.getId()))
            .willReturn(List.of(
                ReservationCountResult.of(1L, LocalTime.of(10, 0), 2L),
                ReservationCountResult.of(2L, LocalTime.of(11, 0), 0L)
            ));
        ReservationSlotService reservationSlotService = createReservationSlotService();

        // when
        List<ReservationSlotResponse> responses = reservationSlotService.getReservationSlots(
            theme.getId(),
            reservationDate.getId()
        );

        // then
        assertThat(responses)
            .extracting(
                ReservationSlotResponse::timeId,
                ReservationSlotResponse::startAt,
                ReservationSlotResponse::waitingNumber
            )
            .containsExactly(
                tuple(1L, LocalTime.of(10, 0), 2L),
                tuple(2L, LocalTime.of(11, 0), 0L)
            );
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약 슬롯을 조회할 수 없다.")
    void throwExceptionWhenThemeDoesNotExist() {
        // given
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 16))
        );
        ReservationSlotService reservationSlotService = createReservationSlotService();

        // when & then
        assertThatThrownBy(() -> reservationSlotService.getReservationSlots(1L, reservationDate.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 테마 입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 날짜로 예약 슬롯을 조회할 수 없다.")
    void throwExceptionWhenDateDoesNotExist() {
        // given
        Theme theme = themeRepository.save(Theme.createWithoutId("공포", "무서운 테마", "theme-url"));
        ReservationSlotService reservationSlotService = createReservationSlotService();

        // when & then
        assertThatThrownBy(() -> reservationSlotService.getReservationSlots(theme.getId(), 1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 날짜 입니다.");
    }

    private ReservationSlotService createReservationSlotService() {
        return new ReservationSlotService(
            themeRepository,
            reservationDateRepository,
            reservationRepository
        );
    }
}
