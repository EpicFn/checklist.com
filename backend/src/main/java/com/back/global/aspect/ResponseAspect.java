package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



@Aspect
@Component
public class ResponseAspect {
    private static final int CUSTOM_AUTH_ERROR_CODE = 499; // Custom status code for specific cases
    private static final int HTTP_UNAUTHORIZED = 401;

    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();

        if (!(proceed instanceof RsData<?> rsData)) {
            return proceed;
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletResponse response = attributes.getResponse();
            if (response != null) {
                if (rsData.code() == CUSTOM_AUTH_ERROR_CODE) {
                    response.setStatus(HTTP_UNAUTHORIZED); // 인증 에러를 401로 변환
                }
                else {
                    response.setStatus(rsData.code());
                }
            }
        } catch (IllegalStateException e) {
            // RequestContextHolder가 현재 스레드에 바인딩되지 않은 경우
            // 예를 들어, 비동기 작업에서 호출된 경우
            // 이 경우에는 아무런 처리를 하지 않고 proceed를 그대로 반환합니다.
            // 로그를 남기거나 다른 처리를 할 수도 있습니다.
        }

        return proceed;
    }
}

