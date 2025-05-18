package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;
import roomescape.presentation.dto.request.AvailableReservationTimeRequest;
import roomescape.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ReservationTimeServiceImpl implements ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeServiceImpl(ReservationTimeRepository reservationTimeRepository,
                                      ReservationRepository reservationRepository,
                                      ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Override
    public List<ReservationTimeResponse> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return ReservationTimeResponse.from(reservationTimes);
    }

    @Override
    @Transactional
    public ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request) {

        LocalTime startAt = request.startAt();
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 시간입니다.");
        }

        ReservationTime reservationTime = ReservationTime.create(request.startAt());
        ReservationTime created = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(created);
    }

    @Override
    @Transactional
    public void deleteReservationTimeById(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalArgumentException("[ERROR] 해당 시간에 이미 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    @Override
    public ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약 시간을 찾을 수 없습니다."));
    }

    @Override
    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(
            AvailableReservationTimeRequest request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당 테마가 존재하지 않습니다."));

        return reservationTimeRepository.findAllAvailableReservationTimes(request.date(), theme);
    }
}
