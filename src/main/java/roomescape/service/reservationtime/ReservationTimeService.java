package roomescape.service.reservationtime;

import org.springframework.stereotype.Service;
import roomescape.domain.BookingStatus;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTime createReservationTime(ReservationTimeSaveRequest request) {
        if (reservationTimeRepository.findByStartAt(request.startAt()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 예약 시간입니다.");
        }

        ReservationTime newReservationTime = request.toEntity(request);
        return reservationTimeRepository.save(newReservationTime);
    }

    public List<ReservationTime> findReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public BookingStatus findTimeSlotsBookingStatus(LocalDate date,
                                                    long themeId) {
        List<ReservationTime> reservedTimes =
                reservationTimeRepository.findReservationByThemeIdAndDate(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return BookingStatus.of(reservedTimes, reservationTimes);
    }

    public void deleteReservationTime(long id) {
        reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간 아이디 입니다."));

        if (reservationRepository.existsByReservationTimeId(id)) {
            throw new IllegalArgumentException("이미 예약중인 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
