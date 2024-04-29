package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.ReservationTimeWithBookStatusResponse;

@Service
public class ReservationTimeService {

    private ReservationTimeRepository reservationTimeRepository;

    ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<ReservationTimeWithBookStatusResponse> findAllWithBookStatus(LocalDate date, Long themeId) {
        return reservationTimeRepository.findByDateAndThemeIdWithBookStatus(date, themeId);
    }

    public ReservationTimeResponse saveReservationTime(ReservationTimeAddRequest reservationTimeAddRequest) {
        if (reservationTimeRepository.existByStartAt(reservationTimeAddRequest.startAt())) {
            throw new DuplicateSaveException("이미 존재하는 예약시간은 추가할 수 없습니다.");
        }
        ReservationTime saved = reservationTimeRepository.save(reservationTimeAddRequest.toReservationTime());
        return new ReservationTimeResponse(saved);
    }

    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
