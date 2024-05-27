package roomescape.service.booking.waiting.module;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.util.DateUtil;

@Service
@Transactional
public class WaitingRegisterService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingRegisterService(WaitingRepository waitingRepository,
                                  ReservationRepository reservationRepository,
                                  ReservationTimeRepository timeRepository,
                                  ThemeRepository themeRepository,
                                  MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Long registerWaiting(ReservationRequest request) {
        Reservation reservation = convertReservation(request);
        validateAddableWaiting(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        addWaiting(savedReservation);
        return savedReservation.getId();
    }

    private Reservation convertReservation(ReservationRequest request) {
        ReservationTime reservationTime = findReservationTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Member member = findMember(request.memberId());
        return request.toEntity(reservationTime, theme, member, Status.WAITING);
    }

    private ReservationTime findReservationTime(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약시간 정보 입니다.",
                        new Throwable("time_id : " + timeId)
                ));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 테마 정보 입니다.",
                        new Throwable("theme_id : " + themeId)
                ));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 사용자 정보 입니다.",
                        new Throwable("member_id : " + memberId)
                ));
    }

    private void addWaiting(Reservation savedReservation) {
        int waitingOrder = reservationRepository.countByDateAndTimeIdAndThemeIdAndStatus(
                savedReservation.getDate(),
                savedReservation.getTime().getId(),
                savedReservation.getTheme().getId(),
                savedReservation.getStatus()
        );
        waitingRepository.save(new Waiting(savedReservation, waitingOrder));
    }

    private void validateAddableWaiting(Reservation reservation) {
        validateUnPassedDate(reservation.getDate(), reservation.getTime().getStartAt());
        validateWaitingDuplicate(reservation);
    }

    private void validateUnPassedDate(LocalDate date, LocalTime time) {
        if (DateUtil.isPastDateTime(date, time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 지나간 날짜와 시간은 예약이 불가능합니다.",
                    new Throwable("생성 예약 시간 : " + date + " " + time)
            );
        }
    }

    private void validateWaitingDuplicate(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId())
        ) {
            throw new IllegalArgumentException(
                    "[ERROR] 이미 사용자에게 등록되거나 대기중인 예약이 있습니다..",
                    new Throwable("생성 예약 대기 정보 : " + reservation)
            );
        }
    }
}
