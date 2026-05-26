package roomescape.domain.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationByNameResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.dto.response.ReservationStatus;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.Status;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralNotFoundException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<ReservationResponseDto> getReservations() {
        List<Reservation> reservations = reservationRepository.findReservationsByDeletedAtIsNull();
        return convertReservationsToDto(reservations);
    }

    public List<ReservationByNameResponseDto> getReservationsByName(String name) {
        List<Reservation> reservations = reservationRepository.findReservationsByNameAndDeletedAtIsNull(name);
        return reservations.stream()
            .map(reservation -> ReservationMapper.toByNameResponseDto(reservation, getStatus(reservation)))
            .toList();
    }

    private ReservationStatus getStatus(Reservation reservation) {
        if (reservation.getStatus() == Status.CANCELED) {
            return ReservationStatus.CANCELED;
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            return ReservationStatus.LOCKED;
        }

        if (reservation.getTime().getDeletedAt() != null || reservation.getTheme().getDeletedAt() != null) {
            return ReservationStatus.EDIT_RECOMMENDED;
        }

        return ReservationStatus.EDITABLE;
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<Reservation> reservations) {
        return reservations.stream()
            .map(ReservationMapper::toResponseDto)
            .toList();
    }

    @Transactional
    public ReservationCreateResponseDto saveReservation(ReservationCreateRequestDto requestDto) {
        Reservation reservation = createReservation(requestDto);

        if (reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme())) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return ReservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    @Transactional
    public ReservationCreateResponseDto updateReservation(Long id, ReservationUpdateRequestDto requestDto) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!existingReservation.getName().equals(requestDto.name())) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }

        if (existingReservation.getStatus() != Status.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        if (existingReservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_UPDATE);
        }

        Reservation updateReservation = createUpdateReservation(existingReservation, requestDto);
        if (reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(
            updateReservation.getDate(), updateReservation.getTime(), updateReservation.getTheme(), id)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }

        try {
            return ReservationMapper.toCreateResponseDto(reservationRepository.update(updateReservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelReservation(Long id, String name) {
        Reservation reservation = reservationRepository.findReservationByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!reservation.getName().equals(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (reservation.getStatus() == Status.CANCELED) {
            throw new GeneralException(ReservationErrorType.ALREADY_CANCELED);
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }

        return ReservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancel()));
    }

    private Reservation createReservation(ReservationCreateRequestDto requestDto) {

        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        Time time = timeRepository.findTimeByIdAndDeletedAtIsNull(requestDto.timeId()).orElse(null);
        if (time == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        Theme theme = themeRepository.findThemeByIdAndDeletedAtIsNull(requestDto.themeId()).orElse(null);
        if (theme == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralNotFoundException(ReservationErrorType.FIELD_RESOURCE_NOT_FOUND, parameterErrorResponses);
        }

        return Reservation.create(requestDto.name(), requestDto.date(), time, theme);
    }

    private Reservation createUpdateReservation(Reservation existingReservation,
        ReservationUpdateRequestDto requestDto) {
        LocalDate date = getUpdateDate(existingReservation, requestDto);
        Time time = getUpdateTime(existingReservation, requestDto);
        Theme theme = getUpdateTheme(existingReservation, requestDto);

        validateUpdateResources(time, theme);

        return Reservation.reconstruct(existingReservation.getId(), existingReservation.getName(), date, time, theme,
            existingReservation.getStatus());
    }

    private LocalDate getUpdateDate(Reservation existingReservation, ReservationUpdateRequestDto requestDto) {
        if (requestDto.date() == null) {
            return existingReservation.getDate();
        }
        return requestDto.date();
    }

    private Time getUpdateTime(Reservation existingReservation, ReservationUpdateRequestDto requestDto) {
        if (requestDto.timeId() == null) {
            return existingReservation.getTime();
        }
        return timeRepository.findTimeByIdAndDeletedAtIsNull(requestDto.timeId()).orElse(null);
    }

    private Theme getUpdateTheme(Reservation existingReservation, ReservationUpdateRequestDto requestDto) {
        if (requestDto.themeId() == null) {
            return existingReservation.getTheme();
        }
        return themeRepository.findThemeByIdAndDeletedAtIsNull(requestDto.themeId()).orElse(null);
    }

    private void validateUpdateResources(Time time, Theme theme) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        if (time == null || time.getDeletedAt() != null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        if (theme == null || theme.getDeletedAt() != null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralNotFoundException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND,
                parameterErrorResponses);
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsReservationByIdAndDeletedAtIsNull(id)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }

        reservationRepository.deleteReservationById(id);
    }

    @Transactional
    public ReservationCreateResponseDto saveWaitingReservation(ReservationCreateRequestDto requestDto) {
        Reservation reservation = createReservation(requestDto);
        Reservation waitingReservation = reservation.toWaiting();

        if (reservationRepository.existsReservation(waitingReservation)) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }

        try {
            return ReservationMapper.toCreateResponseDto(reservationRepository.save(waitingReservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    @Transactional
    public ReservationCancelResponseDto cancelWaitingReservation(Long id, String name) {
        Reservation reservation = reservationRepository.findReservationByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        if (!reservation.getName().equals(name)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }

        if (reservation.getStatus() != Status.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }

        return ReservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancel()));
    }
}
