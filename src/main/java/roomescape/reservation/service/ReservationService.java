package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidRequestException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation create(String name, LocalDate date, Long timeId, Long themeId) {
        validateCreatable(name, date, timeId, themeId);

        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        ReservationSlot slot = ReservationSlot.of(theme, date, time);

        Reservation reservation = Reservation.create(name, slot, LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public void validateCreatable(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        ReservationSlot slot = ReservationSlot.of(theme, date, time);

        Reservation.create(name, slot, LocalDateTime.now());

        if (reservationRepository.existsConflict(name, slot)) {
            throw new ConflictException("이미 같은 날짜, 시간, 테마에 예약 또는 대기가 있습니다.");
        }
    }

    @Transactional
    public Reservation updateDateTime(Long id, String name, LocalDate date, Long timeId) {
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("변경할 예약이 존재하지 않습니다. 예약 목록을 확인해주세요."));

        if (!reservation.isReservedBy(name)) {
            throw new NotFoundException("해당 이름으로 예약을 찾을 수 없습니다. 예약 정보를 확인해주세요.");
        }
        if (reservation.isCanceled()) {
            throw new InvalidRequestException("취소된 예약은 변경할 수 없습니다.");
        }

        ReservationTime time = findTime(timeId);
        ReservationSlot slot = ReservationSlot.of(reservation.getTheme(), date, time);
        if (reservation.isSameSlot(slot)) {
            return reservation;
        }

        if (reservationRepository.existsConflictExcluding(name, slot, reservation.getId())) {
            throw new ConflictException("이미 같은 날짜, 시간, 테마에 예약 또는 대기가 있습니다.");
        }

        Reservation newReservation = Reservation.create(
                reservation.getName(),
                slot,
                now);

        reservationRepository.update(reservation.cancel());

        return reservationRepository.save(newReservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
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

        reservationRepository.update(reservation.cancel());
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> reservationRepository.deleteById(id));
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다. 다른 시간을 선택해주세요."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다. 다른 테마를 선택해주세요."));
    }
}
