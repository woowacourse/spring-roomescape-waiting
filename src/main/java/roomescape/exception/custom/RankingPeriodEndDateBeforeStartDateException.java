package roomescape.exception.custom;

public class RankingPeriodEndDateBeforeStartDateException extends CustomException {

    public RankingPeriodEndDateBeforeStartDateException() {
        super("랭킹 조회 기간의 종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
    }
}
