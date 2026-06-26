# 실용적 예외

## 배경

> `CQS`  
> 명령과 쿼리는 명확히 분리되어야 한다
> - Stack-pop / Insert-return value
>
> `도메인 로직이 쿼리에 녹아 있으면 안 된다`  
> 명시적인 원칙은 아니지만 도메인 로직이 쿼리에 숨어들었을 때의 빈약한 도메인 모델,  
> Fowler의 PoEAA + Anemic Domain Model + Evans의 DDD Repository 등등 다양한 아티클에서의 위험성 지적
> - Reservation / Waiting 객체 내부 Session 조립 책임
> - Waiting 객체 내부 waitingNumber 판별 책임 
 
