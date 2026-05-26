package roomescape.reservation.application.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;

    public ReservationResult save(ReservationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation reservation = request.toEntity(theme.getId(), time.getId(), time.getStartAt());
        reservation.validateReservable(request.now());
        validateDuplicateReservation(reservation);

        Reservation savedReservation;
        try {
            savedReservation = reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }

        return ReservationResult.from(
                savedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time)
        );
    }

    public ReservationResult update(Long reservationId, ReservationUpdateCommand request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation updatedReservation = reservation.updateDateAndTime(request.date(), request.timeId(), time.getStartAt());
        updatedReservation.validateReservable(request.now());

        updateReservation(updatedReservation);

        Theme theme = themeRepository.findById(reservation.getThemeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        return ReservationResult.from(
                updatedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time)
        );
    }

    public void delete(Long id, LocalDateTime now) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        reservation.validateDeletable(now);

        if (reservationRepository.delete(id) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
    }

    private void updateReservation(Reservation reservation) {
        checkAlreadyExistsDateAndTime(reservation);

        if (reservationRepository.update(reservation) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
    }

    private void checkAlreadyExistsDateAndTime(Reservation reservation) {
        if (reservationRepository.existsDuplicateExcluding(reservation)) {
            throw new ConflictException("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }

    private void validateDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsDuplicate(reservation)) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }
    }
}
