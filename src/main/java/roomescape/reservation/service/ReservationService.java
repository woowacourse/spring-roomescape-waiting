package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.global.exception.IllegalReservationDateException;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.MemberReservationAddRequest;
import roomescape.reservation.dto.MemberReservationStatusResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(Long memberId, Long themeId, LocalDate dateFrom,
                                                                      LocalDate dateTo) {
        return reservationRepository.findByMemberIdAndThemeIdAndDateValueBetween(memberId, themeId,
                        dateFrom, dateTo).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MemberReservationStatusResponse> findAllByMemberWithStatus(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(MemberReservationStatusResponse::new)
                .toList();
    }

    public ReservationResponse saveMemberReservation(Long memberId, MemberReservationAddRequest request) {
        if (reservationRepository.existsByDateValueAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId())) {
            throw new DuplicateSaveException("중복되는 예약이 존재합니다.");
        }

        return saveMemberReservation(memberId, request, Status.RESERVED);
    }

    public ReservationResponse saveMemberWaitingReservation(Long memberId, MemberReservationAddRequest request) {
        if (reservationRepository.existsByDateValueAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), memberId)) {
            throw new DuplicateSaveException("이미 회원님이 대기하고 있는 예약이 존재합니다.");
        }
        return saveMemberReservation(memberId, request, Status.WAITING);
    }

    private ReservationResponse saveMemberReservation(Long memberId, MemberReservationAddRequest request, Status status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchRecordException("ID: " + memberId + " 해당하는 회원을 찾을 수 없습니다"));
        ReservationTime reservationTime = getReservationTime(request.timeId());
        validateReservingPastTime(request.date(), reservationTime.getStartAt());
        Theme theme = getTheme(request.themeId());

        Reservation reservation = new Reservation(member, request.date(), reservationTime, theme, status);
        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(saved);
    }

    private void validateReservingPastTime(LocalDate date, LocalTime time) {
        LocalDate nowDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalTime nowTime = LocalTime.now(ZoneId.of("Asia/Seoul"));
        if (date.isBefore(nowDate) || (date.isEqual(nowDate) && time.isBefore(nowTime))) {
            throw new IllegalReservationDateException(
                    nowDate + " " + nowTime + ": 예약 날짜와 시간은 현재 보다 이전일 수 없습니다");
        }
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 예약시간이 존재하지 않습니다 ID: " + timeId));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 테마가 존재하지 않습니다 ID: " + themeId));
    }

    public void removeReservation(long id) {
        reservationRepository.deleteById(id);
    }
}
