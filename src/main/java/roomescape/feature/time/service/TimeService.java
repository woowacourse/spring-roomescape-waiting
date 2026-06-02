package roomescape.feature.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.dto.command.TimeCreateCommand;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.error.type.TimeErrorType;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

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
        return timeRepository.findAll()
            .stream()
            .map(timeMapper::toResponseDto)
            .toList();
    }

    public List<TimeAvailabilityResponseDto> getTimeAvailabilities(LocalDate date, Long themeId) {
        if (!themeRepository.existsThemeByIdAndNotDeleted(themeId)) {
            throw new GeneralParametersException(TimeErrorType.FIELD_RESOURCE_NOT_FOUND,
                List.of(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")));
        }

        List<Long> reservedTimeIds = reservationRepository.findTimeIdsByDateAndThemeIdAndNotDeleted(date, themeId);

        return timeRepository.findAllByNotDeleted()
            .stream()
            .map(time -> timeMapper.toAvailabilityResponseDto(time, !reservedTimeIds.contains(time.getId())))
            .toList();
    }

    @Transactional
    public TimeResponseDto saveTime(TimeCreateCommand command) {
        if (timeRepository.existsTimeByStartAtAndNotDeleted(command.startAt())) {
            throw new GeneralException(TimeErrorType.ALREADY_EXIST_TIME);
        }

        try {
            Time time = Time.create(command.startAt());
            return timeMapper.toResponseDto(timeRepository.save(time));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(TimeErrorType.ALREADY_EXIST_TIME);
        }
    }

    @Transactional
    public void deleteTimeById(Long id) {
        if (!timeRepository.existsTimeByIdAndNotDeleted(id)) {
            throw new GeneralException(TimeErrorType.TIME_NOT_FOUND);
        }

        timeRepository.deleteTimeById(id);
    }
}
