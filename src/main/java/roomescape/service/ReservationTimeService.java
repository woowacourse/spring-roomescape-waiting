package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.REFERENTIAL_INTEGRITY;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeStatus;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationTimeRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationtimeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;

    public ReservationTimeService(ReservationTimeRepository reservationtimeRepository,
                                  ReservationRepository reservationRepository, WaitlistRepository waitlistRepository) {
        this.reservationtimeRepository = reservationtimeRepository;
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
    }

    public List<ReservationTime> getReservationTimes() {
        return reservationtimeRepository.findAll();
    }

    public ReservationTime getReservationTime(Long id) {
        return reservationtimeRepository.getById(id, "존재하지 않는 예약 시간입니다.");
    }

    public List<ReservationTimeStatus> getTimeSlotsWithReservationStatus(LocalDate date, Long themeId) {
        List<ReservationTime> times = getReservationTimes();
        Set<Long> reservedTimeIds = reservationRepository.findReservedTimeIdsByDateAndThemeId(date, themeId);

        return times.stream()
                .map(time -> new ReservationTimeStatus(time, reservedTimeIds.contains(time.getId())))
                .toList();
    }

    @Transactional
    public ReservationTime addReservationTime(ReservationTimeRequest request) {
        Long id = reservationtimeRepository.save(new ReservationTime(request.startAt()));
        return getReservationTime(id);
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        if (reservationRepository.existsByTimeId(id) || waitlistRepository.existsByTimeId(id)) {
            throw new RoomEscapeException(REFERENTIAL_INTEGRITY, "해당 시간을 사용 중인 예약이 존재하여 삭제할 수 없습니다.");
        }

        getReservationTime(id);
        reservationtimeRepository.deleteById(id);
    }
}
