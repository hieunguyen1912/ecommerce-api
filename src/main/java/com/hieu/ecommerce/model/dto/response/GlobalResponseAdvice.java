package com.hieu.ecommerce.model.dto.response;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Chỉ áp dụng cho REST Controllers
        boolean isRestController = returnType.getContainingClass()
                .isAnnotationPresent(RestController.class);

        // Không áp dụng cho Swagger/OpenAPI endpoints
        boolean isSwaggerEndpoint = returnType.getContainingClass()
                .getPackage().getName().contains("springdoc");

        // Không áp dụng cho actuator endpoints
        boolean isActuatorEndpoint = returnType.getContainingClass()
                .getPackage().getName().contains("actuator");

        return isRestController && !isSwaggerEndpoint && !isActuatorEndpoint;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        HttpStatus status = getHttpStatus(response);
        String path = request.getURI().getPath();

        if (body instanceof ApiResponse) return body;         // Đã được wrap
        if (body instanceof String) return body;              // Trả chuỗi đơn giản (vd: text)
        if (isFileDownload(selectedContentType)) return body; // File download
        if (status.is4xxClientError() || status.is5xxServerError()) return body; // Đã handle lỗi

        ApiResponse<Object> wrappedResponse = ApiResponse.success(body);
        wrappedResponse.setPath(path);

        ResponseMessage messageAnn = returnType.getMethodAnnotation(ResponseMessage.class);
        String message = (messageAnn != null) ? messageAnn.value() : "Success";

        wrappedResponse.setMessage(message);

        return wrappedResponse;
    }

    private HttpStatus getHttpStatus(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse) {
            return HttpStatus.valueOf(
                    ((ServletServerHttpResponse) response).getServletResponse().getStatus()
            );
        }
        return HttpStatus.OK;
    }

    private boolean isFileDownload(MediaType contentType) {
        return contentType != null && (
                contentType.equals(MediaType.APPLICATION_OCTET_STREAM) ||
                        contentType.getType().equals("application") &&
                                (contentType.getSubtype().contains("pdf") ||
                                        contentType.getSubtype().contains("excel") ||
                                        contentType.getSubtype().contains("zip"))
        );
    }
}
