package roomescape.service.reservationwait;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationWaitSaveRequest;

@Service
public class ReservationWaitCreateService {

    private final ReservationWaitRepository reservationWaitRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationWaitCreateService(ReservationWaitRepository reservationWaitRepository,
                                        ReservationTimeRepository reservationTimeRepository,
                                        ThemeRepository themeRepository) {
        this.reservationWaitRepository = reservationWaitRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ReservationWait create(ReservationWaitSaveRequest request, Member member) {
        ReservationTime reservationTime = getReservationTime(request.time());
        ReservationWait reservationWait = request.toEntity(
                reservationTime,
                getTheme(request.theme()),
                member);
        validateDateIsFuture(request.date(), reservationTime);
        return reservationWaitRepository.save(reservationWait);
    }

    private ReservationTime getReservationTime(long reservationId) {
        return reservationTimeRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 시간 입니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 테마 입니다."));
    }

    private void validateDateIsFuture(LocalDate date, ReservationTime reservationTime) {
        LocalDateTime localDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("지나간 날짜와 시간에 대한 예약 대기 생성은 불가능합니다.");
        }
    }
}
