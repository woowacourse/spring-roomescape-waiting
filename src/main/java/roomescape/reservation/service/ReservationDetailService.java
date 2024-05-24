package roomescape.reservation.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import roomescape.exception.BadRequestException;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationDetailService {
    private final ReservationDetailRepository detailRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationDetailService(ReservationDetailRepository detailRepository, TimeRepository timeRepository, ThemeRepository themeRepository) {
        this.detailRepository = detailRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationRequest addReservation(ReservationCreateRequest reservationRequest) {
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택하신 테마가 존재하지 않습니다."));
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new BadRequestException("해당 예약 시간이 존재하지 않습니다."));
        validateReservationRequest(reservationRequest, time);

        ReservationDetail reservation = reservationRequest.toReservationDetail(theme, time);
        ReservationDetail savedReservation = detailRepository.save(reservation);
        return new ReservationRequest(reservationRequest.memberId(), savedReservation.getId());
    }

    private void validateReservationRequest(ReservationCreateRequest reservationRequest, Time time) {
        if (reservationRequest.isBeforeDate(LocalDate.now())) {
            throw new BadRequestException("지난 날짜의 예약을 시도하였습니다.");
        }
    }
}
