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
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
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
    private final WaitingRepository waitingRepository;

    public ReservationResult save(ReservationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation reservation = request.toEntity(theme.getId(), time.getId(), time.getStartAt());
        reservation.validateReservable(request.now());

        // 예약이 존재하는지 확인하는 메서드
        // 현재는 예약이 있으면 예외
        // TODO: 그런데 이제는 예약이 있으면 예외가 아니라 Waiting을 만들고 Reservation은 만들면 안됨
        if (reservationRepository.existsDuplicate(reservation)) {
            Waiting waiting = request.toWaiting(theme.getId(), time.getId(), time.getStartAt());
            // 위에서 예약에 대해서 이전 시간 예약인지 검증했지만, 대기도 명시적으로 일단 검증 추가
            waiting.validateReservable(request.now());
            Waiting savedWaiting = waitingRepository.save(waiting);
            Long rank = waitingRepository.getRank(savedWaiting);
            return ReservationResult.waiting(
                    savedWaiting,
                    ThemeResult.from(theme),
                    ReservationTimeResult.from(time),
                    rank
            );
        }

        // 특정 슬롯에 예약이 없는 상황에서 동시에 예약 생성 요청이 들어옴
        Reservation savedReservation;
        try {
            savedReservation = reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            // TODO: 여기서 예외 발생이 아닌, Waiting으로 저장하는 로직 필요
            // 그후 ConcurrencyTest에서 Waiting으로 저장된 것을 테스트해야함.
            // 현재는 기존 예외 발생 로직을 테스트
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }

        return ReservationResult.confirmed(
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

        return ReservationResult.confirmed(
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
        // 예약이 존재하는지 확인하는 메서드
        // 현재는 예약이 있으면 예외
        // TODO: 그런데 이제는 예약이 있으면 예외가 아니라 Waiting을 만들고 Reservation은 만들면 안됨
        if (reservationRepository.existsDuplicate(reservation)) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }
    }
}
