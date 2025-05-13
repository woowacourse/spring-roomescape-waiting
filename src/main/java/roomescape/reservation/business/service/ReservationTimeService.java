package roomescape.reservation.business.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.impl.BadRequestException;
import roomescape.global.exception.impl.NotFoundException;
import roomescape.reservation.business.domain.ReservationTime;
import roomescape.reservation.business.repository.ReservationDao;
import roomescape.reservation.business.repository.ReservationTimeDao;
import roomescape.reservation.presentation.request.ReservationTimeRequest;
import roomescape.reservation.presentation.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(final ReservationDao reservationDao, final ReservationTimeDao reservationTimeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> times = reservationTimeDao.findAll();
        return times.stream()
                .map(ReservationTimeResponse::of)
                .toList();
    }

    public ReservationTimeResponse add(final ReservationTimeRequest requestDto) {
        if (reservationTimeDao.existByTime(requestDto.startAt())) {
            throw new BadRequestException("동일한 시간이 이미 존재합니다.");
        }
        final ReservationTime reservationTime = new ReservationTime(requestDto.startAt());
        final ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);
        return ReservationTimeResponse.of(savedReservationTime);
    }

    public void deleteById(final Long id) {
        if (reservationDao.existByTimeId(id)) {
            throw new BadRequestException("이 시간의 예약이 존재합니다.");
        }
        final int affectedRows = reservationTimeDao.deleteById(id);
        if (affectedRows == 0) {
            throw new NotFoundException("삭제할 예약시간이 없습니다.");
        }
    }
}
