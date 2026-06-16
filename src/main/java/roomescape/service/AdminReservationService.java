package roomescape.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public AdminReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }


    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command) {
        return create(command, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command, ReservationStatus status) {
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

        validateNoConflict(command);

        Reservation reservation = new Reservation(null, command.name(), new ReservationSlot(null, command.date(), time, theme), status);
        ReservationWithWaitingOrder saved = reservationRepository.save(reservation);
        log.info("예약 생성 완료: reservationId={}, name={}, date={}, timeId={}, themeId={}",
                saved.id(), saved.name(), saved.date(), command.timeId(), command.themeId());
        return ReservationResult.from(saved);
    }

    private void validateNoConflict(ReservationCreateCommand command) {
        boolean conflict = reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                command.name(), command.date(), command.timeId(), command.themeId());
        if (conflict) {
            throw new ReservationConflictException(
                    "이미 본인이 예약 또는 대기중인 시간입니다: %s, timeId=%d, themeId=%d"
                            .formatted(command.date(), command.timeId(), command.themeId())
            );
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!reservationRepository.existsById(id)) {
            log.warn("존재하지 않는 예약 삭제 시도: reservationId={}", id);
            throw new ReservationNotFoundException("존재하지 않는 예약입니다: reservationId=" + id);
        }
        reservationRepository.deleteById(id);
    }
}
