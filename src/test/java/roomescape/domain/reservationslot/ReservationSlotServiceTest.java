package roomescape.domain.reservationslot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservationdate.JpaReservationDateRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.JpaThemeRepository;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationSlotServiceTest {

    @Mock
    private JpaReservationSlotRepository reservationSlotRepository;

    @Mock
    private JpaThemeRepository themeRepository;

    @Mock
    private JpaReservationDateRepository reservationDateRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationSlotService reservationSlotService;

    @Test
    @DisplayName("예약 슬롯별 현재 예약 인원을 조회한다.")
    void getReservationSlots() {
        // given
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        ReservationDate reservationDate = ReservationDate.of(1L, LocalDate.of(2026, 5, 16));
        given(themeRepository.findById(theme.getId())).willReturn(Optional.of(theme));
        given(reservationDateRepository.findById(reservationDate.getId())).willReturn(Optional.of(reservationDate));
        given(reservationRepository.countReservation(theme.getId(), reservationDate.getId()))
            .willReturn(List.of(
                ReservationCountResult.of(1L, LocalTime.of(10, 0), 2L),
                ReservationCountResult.of(2L, LocalTime.of(11, 0), 0L)
            ));

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
        Long themeId = 1L;
        Long reservationDateId = 1L;
        given(themeRepository.findById(themeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationSlotService.getReservationSlots(themeId, reservationDateId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 테마 입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 날짜로 예약 슬롯을 조회할 수 없다.")
    void throwExceptionWhenDateDoesNotExist() {
        // given
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Long reservationDateId = 1L;
        given(themeRepository.findById(theme.getId())).willReturn(Optional.of(theme));
        given(reservationDateRepository.findById(reservationDateId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationSlotService.getReservationSlots(theme.getId(), reservationDateId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 날짜 입니다.");
    }

    @Test
    @DisplayName("unique 조건을 위반할 경우 재조회를 한다.")
    void findExistingSlotWhenDuplicateKeyOccurs() {
        // given
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 6, 1));
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        ReservationSlot existingSlot = ReservationSlot.of(1L, date, time, theme);
        given(reservationSlotRepository.findByScheduleToUpdate(time.getId(), date.getId(), theme.getId()))
            .willReturn(Optional.empty())
            .willReturn(Optional.of(existingSlot));
        given(reservationSlotRepository.save(any(ReservationSlot.class)))
            .willThrow(new DataIntegrityViolationException("unique 제약조건을 위반했습니다."));

        // when
        ReservationSlot reservationSlot = reservationSlotService.findOrCreateReservationSlot(date, time, theme);

        // then
        assertThat(reservationSlot).isEqualTo(existingSlot);
    }
}
