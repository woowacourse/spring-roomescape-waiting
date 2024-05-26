package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import roomescape.exception.BadRequestException;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationDetailResponse;
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

    public Long findReservationDetailId(ReservationCreateRequest request) {
        Optional<Long> id = detailRepository.findIdByDateAndThemeIdAndTimeId(
                request.date(),
                request.themeId(),
                request.timeId());

        if(id.isPresent()) {
            return id.get();
        }
        return addReservationDetail(request).id();
    }

    private ReservationDetailResponse addReservationDetail(ReservationCreateRequest reservationRequest) {
        ReservationDetail reservation = toReservationDetail(reservationRequest);
        ReservationDetail detail = detailRepository.save(reservation);
        return ReservationDetailResponse.from(detail);
    }

    private ReservationDetail toReservationDetail(ReservationCreateRequest reservationRequest) {
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택하신 테마가 존재하지 않습니다."));
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new BadRequestException("해당 예약 시간이 존재하지 않습니다."));
        if (reservationRequest.isBeforeDate(LocalDate.now())) {
            throw new BadRequestException("지난 날짜의 예약을 시도하였습니다.");
        }

        return reservationRequest.createReservationDetail(theme, time);
    }
}
