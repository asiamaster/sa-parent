package com.sa.mvc.boot;

import com.alibaba.fastjson.JSON;
import com.sa.domain.BaseOutput;
import com.sa.exception.InternalException;
import com.sa.util.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@ControllerAdvice
@ConditionalOnExpression("'${globalExceptionHandler.enable}'=='true'")
public class GlobalExceptionHandler {



































    @ExceptionHandler(InternalException.class)
    public String internalExceptionHandler(HttpServletRequest request, HttpServletResponse response, InternalException e) throws IOException {
        e.printStackTrace();

        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(e.getMessage())));
            return null;
        }

        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", e);
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        return JSON.toJSONString(BaseOutput.failure(e.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public String defultExcepitonHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        Exception exception = e;
        e.printStackTrace();
        String exMsg = exception.getMessage();

        if(e.getCause() != null && "com.alibaba.csp.sentinel.slots.block.flow.FlowException".equals(e.getCause().toString())){
            exception = (Exception)e.getCause();
            exMsg = "服务开启限流保护,请稍后再试!";
        }

        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
            return null;
        }

        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", exception);
            request.setAttribute("exMsg", exMsg);

            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
        return null;
    }

}