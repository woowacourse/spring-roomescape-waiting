package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.ExistReservationException;
import roomescape.exception.NotExistException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.service.dto.output.ReservationTimeOutput;

import java.util.List;

import static roomescape.exception.ExceptionDomainType.RESERVATION_TIME;


@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository, final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeOutput createReservationTime(final ReservationTimeInput input) {
        final ReservationTime reservationTime = input.toReservationTime();

        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new AlreadyExistsException(RESERVATION_TIME, reservationTime.getStartAtAsString());
        }

        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeOutput.toOutput(savedReservationTime);
    }

    public List<ReservationTimeOutput> getAllReservationTimes() {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationTimeOutput.toOutputs(reservationTimes);
    }

    public List<AvailableReservationTimeOutput> getAvailableTimes(final AvailableReservationTimeInput input) {
        final List<ReservationTime> alreadyBookedReservationTimes =
                reservationRepository.getReservationByThemeIdAndDateValue(input.themeId(), input.date())
                        .stream()
                        .map(ReservationInfo::getTime)
                        .toList();
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return AvailableReservationTimeOutput.toOutputs(reservationTimes, alreadyBookedReservationTimes);
    }

    public void deleteReservationTime(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new ExistReservationException(RESERVATION_TIME, id);
        }
        if (reservationTimeRepository.deleteReservationTimeById(id) == 0) {
            throw new NotExistException(RESERVATION_TIME, id);
        }
    }
}
