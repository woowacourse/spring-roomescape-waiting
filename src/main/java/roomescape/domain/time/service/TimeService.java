package roomescape.domain.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.request.TimeCreateRequestDto;
import roomescape.domain.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.error.type.TimeErrorType;
import roomescape.domain.time.mapper.TimeMapper;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralNotFoundException;

@Service
public class TimeService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final TimeMapper timeMapper;

    public TimeService(ReservationRepository reservationRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, TimeMapper timeMapper) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.timeMapper = timeMapper;
    }

    public List<TimeResponseDto> getTimes() {
        return timeRepository.findAllByDeletedAtIsNull()
            .stream()
            .map(timeMapper::toResponseDto)
            .toList();
    }

    public List<TimeAvailabilityResponseDto> getTimeAvailabilities(LocalDate date, Long themeId) {
        if (!themeRepository.existsThemeByIdAndDeletedAtIsNull(themeId)) {
            throw new GeneralNotFoundException(TimeErrorType.FIELD_RESOURCE_NOT_FOUND,
                List.of(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")));
        }

        List<Long> reservedTimeIds = reservationRepository.findTimeIdsByDateAndThemeIdAndDeletedAtIsNull(date, themeId);

        return timeRepository.findAllByDeletedAtIsNull()
            .stream()
            .map(time -> timeMapper.toAvailabilityResponseDto(time, !reservedTimeIds.contains(time.getId())))
            .toList();
    }

    @Transactional
    public TimeResponseDto saveTime(TimeCreateRequestDto requestDto) {
        if (timeRepository.existsTimeByStartAtAndDeletedAtIsNull(requestDto.startAt())) {
            throw new GeneralException(TimeErrorType.ALREADY_EXIST_TIME);
        }

        try {
            Time time = Time.create(requestDto.startAt());
            return timeMapper.toResponseDto(timeRepository.save(time));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(TimeErrorType.ALREADY_EXIST_TIME);
        }
    }

    @Transactional
    public void deleteTimeById(Long id) {
        if (!timeRepository.existsTimeByIdAndDeletedAtIsNull(id)) {
            throw new GeneralException(TimeErrorType.TIME_NOT_FOUND);
        }

        timeRepository.deleteTimeById(id);
    }
}
