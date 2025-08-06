package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 글로벌 예외 핸들러 클래스
 * 각 예외에 대한 적절한 HTTP 상태 코드와 메시지를 포함한 응답 반환
 * 400: Bad Request
 * 404: Not Found
 * 500: Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ServiceException: 서비스 계층에서 발생하는 커스텀 예외
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException ex) {
        RsData<Void> rsData = ex.getRsData();
        int statusCode = rsData.code();

        return ResponseEntity.status(statusCode).body(rsData);
    }

    // NoSuchElementException: 데이터가 존재하지 않을 때 발생하는 예외
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "해당 데이터가 존재하지 않습니다.";

        return new ResponseEntity<>(
                RsData.of(
                        404,
                        errorMessage
                ),
                NOT_FOUND
        );
    }

    // ConstraintViolationException: 제약 조건(@NotNull, @Size 등)을 어겼을 때 발생하는 예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handle(ConstraintViolationException ex) {
        // 메시지 형식: <필드명>-<검증어노테이션명>-<검증실패메시지>
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.split("\\.", 2)[1] : path;
                    String[] messageTemplateBits = violation.getMessageTemplate()
                            .split("\\.");
                    String code = messageTemplateBits[messageTemplateBits.length - 2];
                    String _message = violation.getMessage();

                    return "%s-%s-%s".formatted(field, code, _message);
                })
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }

    // MethodArgumentNotValidException: @Valid 유효성 검사 실패 시 발생하는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        // 메시지 형식: <필드명>-<검증어노테이션명>-<검증실패메시지>
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }

    // HttpMessageNotReadableException: 요청 본문이 올바르지 않을 때 발생하는 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                RsData.of(
                        400,
                        "요청 본문이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    // MissingRequestHeaderException: 필수 요청 헤더가 누락되었을 때 발생하는 예외
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RsData<Void>> handle(MissingRequestHeaderException ex) {
        // 메시지 형식: <헤더명>-NotBlank-<에러메시지>
        String message = "%s-%s-%s".formatted(
                ex.getHeaderName(),
                "NotBlank",
                ex.getLocalizedMessage()
        );

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }
    // MethodArgumentTypeMismatchException: 요청 파라미터의 타입이 일치하지 않을 때 발생하는 예외
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RsData<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
      String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다. 요구되는 타입: %s",
        ex.getName(),
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음");

      return new ResponseEntity<>(
          RsData.of(
              400,
              message
          ),
          BAD_REQUEST
      );
    }

    //MissingServletRequestPartException: 요청된 multipart 요청에서 필수 파트가 누락되었을 때 발생하는 예외
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RsData<Void>> handle(MissingServletRequestPartException ex) {
        String message = "필수 multipart 파트 '%s'가 존재하지 않습니다.".formatted(ex.getRequestPartName());
        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }

    // @PreAuthorize 권한 에러
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<RsData<Void>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
      return new ResponseEntity<>(
          RsData.of(403, "권한이 없습니다."),
          HttpStatus.FORBIDDEN
      );
    }

    // AccessDeniedException 에러 핸들러
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RsData<Void>> handleAccessDenied(AccessDeniedException ex) {
      return new ResponseEntity<>(
          RsData.of(403, "권한이 없습니다."),
          HttpStatus.FORBIDDEN
      );
    }

}