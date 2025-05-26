package roomescape.waiting.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.exception.NotFoundReservationTimeException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.NotFoundThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.dto.WaitingRequestDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.exception.NotFoundWaitingException;
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

    public List<WaitingResponseDto> findAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingService::convertWaitingResponseDto)
                .toList();
    }

    public void delete(Long waitingId) {
        validateExistsById(waitingId);
        waitingRepository.deleteById(waitingId);
    }

    private static WaitingResponseDto convertWaitingResponseDto(Waiting savedWaiting) {
        return WaitingResponseDto.of(savedWaiting);
    }

    private void validateExistsById(Long waitingId) {
        if (!waitingRepository.existsById(waitingId)) {
            throw new NotFoundWaitingException();
        }
    }
}
