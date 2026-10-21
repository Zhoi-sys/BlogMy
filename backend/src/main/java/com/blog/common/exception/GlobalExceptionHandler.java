package com.blog.common.exception;

import com.blog.common.api.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。
 *
 * <p>将业务异常、参数校验失败、权限失败和未知异常统一转换为
 * {@link Result} JSON，并保留正确的 HTTP 状态码。</p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理显式抛出的业务异常。
     *
     * @param ex 业务异常
     * @return 对应 HTTP 状态与业务错误码的响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理请求体/表单 Bean Validation 失败。
     *
     * @param ex 校验异常
     * @return 400 与首个字段错误信息
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidation(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不合法";
        return ResponseEntity.badRequest().body(Result.error(40001, message));
    }

    /**
     * 处理方法参数上的约束校验失败。
     *
     * @param ex 方法参数校验异常
     * @return 400 与首个错误信息
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result<Void>> handleMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getAllErrors().isEmpty()
                ? "请求参数不合法"
                : ex.getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.error(40001, message));
    }

    /**
     * 处理路径/查询参数约束校验失败。
     *
     * @param ex 约束异常
     * @return 400 与首个错误信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("请求参数不合法");
        return ResponseEntity.badRequest().body(Result.error(40001, message));
    }

    /**
     * 处理请求体不可读、参数类型错误和缺少必要查询参数。
     *
     * @param ex 解析/绑定异常
     * @return 400 统一格式错误响应
     */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Result<Void>> handleUnreadable(Exception ex) {
        return ResponseEntity.badRequest().body(Result.error(40001, "请求格式不正确"));
    }

    /**
     * 处理不支持的 HTTP 方法。
     *
     * @param ex 方法不支持异常
     * @return 405 响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.error(40500, "不支持该请求方法"));
    }

    /**
     * 处理数据库唯一键冲突。
     *
     * @param ex 唯一键冲突异常
     * @return 409 响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicate(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(40900, "数据已存在，请勿重复操作"));
    }

    /**
     * 处理方法级权限校验失败。
     *
     * @param ex 权限异常
     * @return 403 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(40300, "没有权限执行此操作"));
    }

    /**
     * 处理静态资源或路由不存在。
     *
     * @param ex 资源不存在异常
     * @return 404 响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(40400, "资源不存在"));
    }

    /**
     * 兜底处理未知异常并记录错误日志。
     *
     * @param ex 未知异常
     * @return 500 通用错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(50000, "服务暂时不可用，请稍后再试"));
    }
}
