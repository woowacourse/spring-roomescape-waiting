package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class AdminReservationService {

    private static final Logger log = LoggerFactory.getLogger(AdminReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public AdminReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }


    public List<ReservationResult> findAll() {
        return reservationRepository.findAllActive().stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 생성 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException(
                            "존재하지 않는 시간입니다: timeId=" + command.timeId());
                });

        return reservationRepository.executeWithThemeLock(command.themeId(), (lockedTheme, writer) -> {
            Theme theme = lockedTheme.orElseThrow(() -> {
                log.warn("존재하지 않는 테마로 예약 생성 시도: themeId={}", command.themeId());
                return new ThemeNotFoundException(
                        "존재하지 않는 테마입니다: themeId=" + command.themeId());
            });

            validateNoConflict(command);

            ReservationStatus status = decideStatus(command.date(), command.timeId(), command.themeId());
            Reservation reservation = new Reservation(null, command.reserverName(), command.date(), time, theme, status);
            ReservationWithWaitingOrder saved = writer.save(reservation);
            log.info("예약 생성 완료: reservationId={}, reserverName={}, date={}, timeId={}, themeId={}, status={}",
                    saved.id(), saved.reserverName(), saved.date(), command.timeId(), command.themeId(), saved.status());
            return ReservationResult.from(saved);
        });
    }

    private ReservationStatus decideStatus(LocalDate date, Long timeId, Long themeId) {
        boolean alreadyConfirmed = reservationRepository.existsActiveConfirmed(date, timeId, themeId);
        return alreadyConfirmed ? ReservationStatus.WAITING : ReservationStatus.CONFIRMED;
    }

    private void validateNoConflict(ReservationCreateCommand command) {
        boolean conflict = reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                command.reserverName(), command.date(), command.timeId(), command.themeId());
        if (conflict) {
            throw new ReservationConflictException(
                    "이미 본인이 예약 또는 대기중인 시간입니다: %s, timeId=%d, themeId=%d"
                            .formatted(command.date(), command.timeId(), command.themeId())
            );
        }
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 예약 취소 시도: reservationId={}", id);
                    return new ReservationNotFoundException("존재하지 않는 예약입니다: reservationId=" + id);
                });
        reservationRepository.executeWithThemeLock(reservation.getTheme().getId(), (lockedTheme, writer) -> {
            Reservation current = reservationRepository.findById(id)
                    .orElseThrow(() -> new ReservationNotFoundException(
                            "존재하지 않는 예약입니다: reservationId=" + id));
            if (current.isCanceled()) {
                return null;
            }
            writer.cancel(current.getId());
            if (current.isConfirmed()) {
                boolean promoted = writer.promoteEarliestWaiting(
                        current.getDate(),
                        current.getTime().getId(),
                        current.getTheme().getId()
                );
                log.info("예약 취소 후 승급 처리: reservationId={}, promoted={}", current.getId(), promoted);
            }
            return null;
        });
    }
}
