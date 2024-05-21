package roomescape.service.reservation;

import static roomescape.domain.ReservationWaitStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.request.ReservationSaveRequest;

@Service
public class ReservationCreateService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitRepository reservationWaitRepository;

    public ReservationCreateService(ReservationRepository reservationRepository,
                                    ReservationTimeRepository reservationTimeRepository,
                                    ThemeRepository themeRepository,
                                    MemberRepository memberRepository,
                                    ReservationWaitRepository reservationWaitRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationWaitRepository = reservationWaitRepository;
    }

    @Transactional
    public Reservation create(ReservationAdminSaveRequest request) {
        Reservation reservation = request.toEntity(
                getReservationTime(request.timeId()),
                getTheme(request.themeId()),
                getMember(request.memberId()));
        return saveReservation(reservation);
    }

    @Transactional
    public Reservation create(ReservationSaveRequest request, Member member) {
        Reservation reservation = request.toEntity(
                getReservationTime(request.timeId()),
                getTheme(request.themeId()),
                member);
        return saveReservation(reservation);
    }

    private Reservation saveReservation(Reservation request) {
        validateAlreadyWait(
                request.getDate(),
                request.getReservationTime().getId(),
                request.getTheme().getId(),
                request.getMember().getId());
        validateDateIsFuture(request.getDate(), request.getReservationTime());
        validateAlreadyBooked(request.getDate(), request.getReservationTime().getId(), request.getTheme().getId());
        return reservationRepository.save(request);
    }

    private ReservationTime getReservationTime(long reservationId) {
        return reservationTimeRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 시간 입니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 테마 입니다."));
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 사용자입니다."));
    }

    private void validateAlreadyWait(LocalDate date, long timeId, long themeId, long memberId) {
        if (reservationWaitRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(date, timeId, themeId,
                memberId, WAITING)) {
            throw new InvalidRequestException("이미 예약 대기 중입니다.");
        }
    }

    private void validateAlreadyBooked(LocalDate date, long timeId, long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new InvalidRequestException("해당 시간에 이미 예약된 테마입니다.");
        }
    }

    private void validateDateIsFuture(LocalDate date, ReservationTime reservationTime) {
        LocalDateTime localDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("지나간 날짜와 시간에 대한 예약 생성은 불가능합니다.");
        }
    }
}
