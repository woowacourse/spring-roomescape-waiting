package roomescape.reservationtime.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);

        return save(reservationTime);
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new ConflictException("예약이 존재하는 시간은 삭제할 수 없습니다. 먼저 해당 예약들을 삭제해주세요.");
        }
        reservationTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeAvailability> findAvailableTimes(LocalDate date, Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new NotFoundException("선택한 테마가 존재하지 않습니다. 다른 테마를 선택해주세요.");
        }

        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        Set<ReservationTime> reservedTimes = reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toCollection(HashSet::new));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> new ReservationTimeAvailability(
                        reservationTime,
                        !reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    private ReservationTime save(ReservationTime reservationTime) {
        try {
            return reservationTimeRepository.save(reservationTime);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("이미 등록된 예약 시간입니다. 다른 시간을 입력해주세요.");
        }
    }
}
