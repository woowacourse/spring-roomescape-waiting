package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.reservationtime.exception.NotFoundReservationTimeException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.ThemeResponseDto;
import roomescape.theme.exception.NotFoundThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.dto.WaitingRequestDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponseDto create(WaitingRequestDto requestDto, User member) {
        Waiting waiting = convertWaiting(requestDto, member);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return convertWaitingResponseDto(savedWaiting);
    }

    private Waiting convertWaiting(WaitingRequestDto requestDto, User member) {
        ReservationTime reservationTime = reservationTimeRepository.findById(requestDto.timeId())
                .orElseThrow(NotFoundReservationTimeException::new);
        Theme theme = themeRepository.findById(requestDto.themeId())
                .orElseThrow(NotFoundThemeException::new);
        return requestDto.toEntity(reservationTime, theme, member);
    }

    private static WaitingResponseDto convertWaitingResponseDto(Waiting savedWaiting) {
        ReservationTime reservationTime = savedWaiting.getTime();
        ReservationTimeResponseDto timeResponseDto = ReservationTimeResponseDto.of(reservationTime);

        Theme theme = savedWaiting.getTheme();
        ThemeResponseDto themeResponseDto = ThemeResponseDto.of(theme);
        return WaitingResponseDto.of(savedWaiting, timeResponseDto, themeResponseDto);
    }
}
