package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.will;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingListServiceTest {

    @Mock
    WaitingListRepository waitingListRepository;
    @Mock
    ThemeRepository themeRepository;
    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    WaitingListService waitingListService;

    @Test
    void 예약_대기_생성() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));
        WaitingList waitingList = WaitingList.create(date, name, theme, reservationTime);
        given(waitingListRepository.save(any(WaitingList.class))).willReturn(waitingList.withId(1));

        // when
        WaitingListResult result = waitingListService.create(createCommand);

        // then
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.date()).isEqualTo(date);
        Assertions.assertThat(result.timeId()).isEqualTo(timeId);
        Assertions.assertThat(result.themeId()).isEqualTo(themeId);
    }

    @Test
    void 예약대기_신청하려는_테마가_없으면_예외발생 () {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );
        given(themeRepository.findById(themeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
    }

    @Test
    void 예약대기_신청하려는_시간이_없으면_예외발생() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
    }
}
