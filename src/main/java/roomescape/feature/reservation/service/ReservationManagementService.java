package roomescape.feature.reservation.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@Service
public class ReservationManagementService implements ReservationService, WaitingService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationMapper reservationMapper;

    public ReservationManagementService(ReservationRepository reservationRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public List<ReservationResponseDto> getReservations() {
        List<Reservation> reservations = reservationRepository.findAllReservations();
        return convertReservationsToDto(reservations);
    }

    private List<ReservationResponseDto> convertReservationsToDto(List<Reservation> reservations) {
        return reservations.stream()
            .map(reservation -> reservationMapper.toResponseDto(reservation, getWaitingNumber(reservation)))
            .toList();
    }

    private Integer getWaitingNumber(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            return null;
        }

        return reservationRepository.countByIdLessThanEqualAndDateAndTimeAndTheme(reservation.getId(),
            reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    @Override
    public List<ReservationResponseDto> getReservationsByName(ReserverName name) {
        List<Reservation> reservations = reservationRepository.findReservationsByNameAndNotDeleted(name);
        return reservations.stream()
            .map(reservation -> reservationMapper.toResponseDto(reservation, getWaitingNumber(reservation)))
            .toList();
    }

    @Override
    @Transactional
    public ReservationCreateResponseDto saveReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.ACTIVE);

        validateNotReservedByOther(reservation);

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private Reservation createReservation(ReservationCreateCommand command, ReservationStatus status) {
        List<ParameterErrorResponseDto> parameterErrorResponses = new ArrayList<>();

        Time time = timeRepository.findTimeByIdAndNotDeleted(command.timeId()).orElse(null);
        if (time == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."));
        }

        Theme theme = themeRepository.findThemeByIdAndNotDeleted(command.themeId()).orElse(null);
        if (theme == null) {
            parameterErrorResponses.add(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."));
        }

        if (!parameterErrorResponses.isEmpty()) {
            throw new GeneralParametersException(ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
                parameterErrorResponses);
        }

        return Reservation.create(command.name(), command.date(), time, theme, status);
    }

    @Override
    @Transactional
    public ReservationCreateResponseDto updateReservation(Long id, ReservationUpdateCommand command) {
        Reservation existingReservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        Time newTime = timeRepository.findTimeByIdAndNotDeleted(command.timeId())
            .orElseThrow(() -> new GeneralException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND));
        Theme newTheme = themeRepository.findThemeByIdAndNotDeleted(command.themeId())
            .orElseThrow(() -> new GeneralException(ReservationErrorType.UPDATE_FIELD_RESOURCE_NOT_FOUND));

        Reservation updated = existingReservation.update(command.name(), command.date(), newTime, newTheme);

        validateNotReservedByOther(updated);

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.update(updated));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    @Override
    @Transactional
    public ReservationCancelResponseDto cancelReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        return reservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancelActive(name)));
    }

    @Override
    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsReservationByIdAndNotDeleted(id)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }

        reservationRepository.deleteReservationById(id);
    }

    @Override
    @Transactional
    public ReservationCreateResponseDto saveWaitingReservation(ReservationCreateCommand command) {
        Reservation reservation = createReservation(command, ReservationStatus.WAITING);

        validateNotAlreadyWaitingByMySelf(reservation);
        validateNotReservedByMyself(reservation);
        validateAlreadyReserved(reservation);

        try {
            return reservationMapper.toCreateResponseDto(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    @Override
    @Transactional
    public ReservationCancelResponseDto cancelWaitingReservation(Long id, ReserverName name) {
        Reservation reservation = reservationRepository.findReservationByIdAndNotDeleted(id)
            .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

        return reservationMapper.toCancelResponseDto(reservationRepository.update(reservation.cancelWaiting(name)));
    }

    private void validateNotReservedByOther(Reservation reservation) {
        if (reservationRepository.existsReservationByDateAndTimeAndThemeAndNotDeleted(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme())) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private void validateNotReservedByMyself(Reservation reservation) {
        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.ACTIVE)) {
            throw new GeneralException(ReservationErrorType.ALREADY_RESERVED);
        }
    }

    private void validateNotAlreadyWaitingByMySelf(Reservation reservation) {
        if (reservationRepository.existsReservationAndStatus(reservation, ReservationStatus.WAITING)) {
            throw new GeneralException(ReservationErrorType.ALREADY_WAITING);
        }
    }

    private void validateAlreadyReserved(Reservation reservation) {
        boolean alreadyReserved = reservationRepository.existsReservationByDateAndTimeAndThemeAndNotDeleted(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );

        if (!alreadyReserved) {
            throw new GeneralException(ReservationErrorType.NOT_RESERVED);
        }
    }
}
