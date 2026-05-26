package roomescape.holiday.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.holiday.exception.HolidayNotFoundException;
import roomescape.holiday.repository.HolidayRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Test
    @DisplayName("삭제할 휴일이 없으면 404 반환")
    void delete_throwsException_whenHolidayNotFound() {
        long id = 1L;
        when(holidayRepository.deleteById(id)).thenReturn(false);
        HolidayService holidayService = new HolidayServiceImpl(holidayRepository);

        assertThatThrownBy(() -> holidayService.delete(id))
                .isInstanceOf(HolidayNotFoundException.class);

        verify(holidayRepository).deleteById(id);
    }

}
