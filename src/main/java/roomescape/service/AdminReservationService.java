package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Transactional(readOnly = true)
@Service
public class AdminReservationService {

    private static final Logger log = LoggerFactory.getLogger(AdminReservationService.class);

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public AdminReservationService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }


    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult reserveOnSlot(ReservationCreateCommand command) {
        return book(command);
    }

    @Transactional
    public ReservationResult waitOnSlot(ReservationCreateCommand command) {
        return book(command);
    }

    private ReservationResult book(ReservationCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 생성 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException("존재하지 않는 시간입니다: timeId=" + command.timeId());
                });

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 테마로 예약 생성 시도: themeId={}", command.themeId());
                    return new ThemeNotFoundException("존재하지 않는 테마입니다: themeId=" + command.themeId());
                });

        Reservation reservation = new Reservation(null, command.name(), command.date(), time, theme);

        validateDuplicate(command);

        if (!reservationRepository.hasReservationOnSlot(command.date(), command.timeId(), command.themeId())) {
            ReservationWithWaitingOrder saved = reservationRepository.save(reservation);
            log.info("예약 생성 완료: reservationId={}, name={}, date={}, timeId={}, themeId={}",
                    saved.id(), saved.name(), saved.date(), command.timeId(), command.themeId());
            return ReservationResult.from(saved);
        }

        Reservation savedWaiting = waitingRepository.save(reservation);
        long waitingCount = waitingRepository.countWaitingsBefore(savedWaiting);
        log.info("예약 대기 생성 완료: waitingId={}, name={}, date={}, waitingOrder={}",
                savedWaiting.getId(), savedWaiting.getName(), savedWaiting.getDate(), waitingCount + 1);

        ReservationWithWaitingOrder reservationWithWaitingOrder =
                ReservationWithWaitingOrder.from(savedWaiting, waitingCount + 1);
        return ReservationResult.from(reservationWithWaitingOrder);
    }

    private void validateDuplicate(ReservationCreateCommand command) {
        if (reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(command.name(), command.date(), command.timeId(), command.themeId())
            || waitingRepository.hasWaitingOnSlot(command.name(), command.date(), command.timeId(), command.themeId())) {
            throw new ReservationConflictException("이미 본인이 예약 또는 대기 중인 슬롯입니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            reservationRepository.deleteById(id);
            log.info("예약 삭제 완료: reservationId={}", id);
            promoteWaitingIfExist(reservation.get());
            return;
        }

        Optional<Reservation> waiting = waitingRepository.findById(id);
        if (waiting.isPresent()) {
            waitingRepository.deleteById(id);
            log.info("대기 삭제 완료: waitingId={}", id);
            return;
        }

        log.warn("존재하지 않는 예약/대기 삭제 시도: id={}", id);
        throw new ReservationNotFoundException("존재하지 않는 예약입니다: id=" + id);
    }

    private void promoteWaitingIfExist(Reservation deletedReservation) {
        waitingRepository.findFirstWaiting(
                deletedReservation.getDate(),
                deletedReservation.getTime().getId(),
                deletedReservation.getTheme().getId()
        ).ifPresent(firstWaiting -> {
            waitingRepository.deleteById(firstWaiting.getId());
            reservationRepository.save(firstWaiting);
            log.info("대기 승격 완료: waitingId={} -> reservation", firstWaiting.getId());
        });
    }
}
