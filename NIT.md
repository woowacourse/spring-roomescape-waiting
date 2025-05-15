### 토큰 만료 시 쿠키 삭제 방법

아래의 링크에 자세히 설명되어 있습니다.

[쿠키 삭제](https://stackoverflow.com/a/5285982)

```java
@RestController
@RequestMapping("/logout")
public class LogoutController {

    @PostMapping
    public ResponseEntity<Void> logout() {
        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header("Location", "/")
                .header("Set-Cookie", "token=deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT")
                .build();
    }
}
```
