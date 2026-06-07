package roomescape.reservation.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidRequestException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationEntry;
import roomescape.reservation.domain.ReservationHistory;
import roomescape.reservation.domain.ReservationSequence;
import roomescape.reservation.repository.ReservationHistoryRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationHistoryRepository reservationHistoryRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationHistoryRepository = reservationHistoryRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationEntry> findAll() {
        return ReservationSequence.entriesOf(reservationRepository.findAll());
    }

    @Transactional
    public Reservation create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);

        Reservation reservation = Reservation.create(name, date, time, theme, LocalDateTime.now());

        return save(reservation);
    }

    @Transactional
    public Reservation updateDateTime(Long id, String name, LocalDate date, Long timeId) {
        Reservation reservation = findReservation(id);

        if (!reservation.isReservedBy(name)) {
            throw new NotFoundException("해당 이름으로 예약을 찾을 수 없습니다. 예약 정보를 확인해주세요.");
        }
        ReservationTime time = findTime(timeId);

        if (reservation.hasSameDateTime(date, time)) {
            return reservation;
        }

        Reservation updatedReservation = reservation.updateDateTime(date, time, LocalDateTime.now());
        return updateReservationDateTime(updatedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationEntry> findByName(String name) {
        return ReservationSequence.entriesOf(reservationRepository.findAllForName(name))
                .stream()
                .filter(entry -> entry.reservation().isReservedBy(name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationEntry> findCanceledByName(String name) {
        return reservationHistoryRepository.findByName(name)
                .stream()
                .map(ReservationHistory::canceled)
                .toList();
    }

    @Transactional
    public void cancel(Long id, String name) {
        Optional<Reservation> foundReservation = reservationRepository.findById(id);
        if (foundReservation.isEmpty()) {
            return;
        }

        Reservation reservation = foundReservation.get();
        if (!reservation.isReservedBy(name)) {
            throw new NotFoundException("해당 이름으로 예약을 찾을 수 없습니다. 예약 정보를 확인해주세요.");
        }
        if (reservation.isPast(LocalDateTime.now())) {
            throw new InvalidRequestException("이미 지난 예약은 취소할 수 없습니다.");
        }

        saveHistory(reservation.getId());
        reservationRepository.deleteById(reservation.getId());
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> reservationRepository.deleteById(id));
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요."));
    }

    private void saveHistory(Long reservationId) {
        try {
            reservationHistoryRepository.save(reservationId);
        } catch (DuplicateKeyException exception) {
            return;
        }
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다. 다른 시간을 선택해주세요."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다. 다른 테마를 선택해주세요."));
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("이미 같은 날짜, 시간, 테마에 예약 또는 대기가 있습니다.");
        } catch (DataIntegrityViolationException exception) {
            if (hasMissingReservationDependency(reservation)) {
                throw new NotFoundException("선택한 예약 시간 또는 테마가 존재하지 않습니다. 다른 예약 정보를 선택해주세요.");
            }
            throw exception;
        }
    }

    private Reservation updateReservationDateTime(Reservation reservation) {
        try {
            return reservationRepository.updateDateTime(reservation)
                    .orElseThrow(() -> new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요."));
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("이미 같은 날짜, 시간, 테마에 예약 또는 대기가 있습니다.");
        } catch (DataIntegrityViolationException exception) {
            if (hasMissingTime(reservation)) {
                throw new NotFoundException("선택한 예약 시간이 존재하지 않습니다. 다른 시간을 선택해주세요.");
            }
            throw exception;
        }
    }

    private boolean hasMissingReservationDependency(Reservation reservation) {
        return hasMissingTime(reservation) || hasMissingTheme(reservation);
    }

    private boolean hasMissingTime(Reservation reservation) {
        Long timeId = reservation.getSlot().time().getId();
        return !reservationTimeRepository.existsById(timeId);
    }

    private boolean hasMissingTheme(Reservation reservation) {
        Long themeId = reservation.getSlot().theme().getId();
        return !themeRepository.existsById(themeId);
    }
}
