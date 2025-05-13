package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.time.AvailableReservationTimeResponse;
import roomescape.dto.time.ReservationTimeCreateRequest;
import roomescape.dto.time.ReservationTimeResponse;
import roomescape.exception.ConstrainedDataException;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse createReservationTime(final ReservationTimeCreateRequest requestDto) {
        ReservationTime requestTime = requestDto.createWithoutId();
        try {
            ReservationTime savedTime = reservationTimeRepository.save(requestTime)
                    .orElseThrow(() -> new IllegalStateException("[ERROR] 예약시간을 저장할 수 없습니다. 관리자에게 문의해 주세요."));

            return ReservationTimeResponse.from(savedTime);
        } catch (DuplicateKeyException e) {
            throw new DuplicateContentException("[ERROR] 이미 동일한 예약 시간이 존재합니다.");
        }
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        return allReservationTime.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservationsOnDate = reservationRepository.findByDateAndTheme(date, themeId);

        List<ReservationTime> reservedTimes = reservationsOnDate.stream()
                .map(Reservation::getTime)
                .toList();

        return allReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    public void deleteReservationTimeById(final Long id) {
        try {
            int deletedReservationCount = reservationTimeRepository.deleteById(id);

            if (deletedReservationCount == 0) {
                throw new NotFoundException("[ERROR] 등록된 예약 시간 번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new ConstrainedDataException("[ERROR] 해당 시간에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요.");
        }
    }
}
