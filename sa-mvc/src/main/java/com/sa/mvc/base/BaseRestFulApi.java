package com.sa.mvc.base;


import com.sa.constant.ResultCode;
import com.sa.domain.BaseOutput;
import com.sa.exception.DataErrorException;
import com.sa.exception.NotAuthException;
import com.sa.exception.ParamErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


public class BaseRestFulApi {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseRestFulApi.class);

    @Autowired
    HttpServletRequest request;

    public static BaseOutput<?> handleException(BaseOutput<?> out, Exception e){
        if(out == null){
            out = BaseOutput.failure();
        }
        if(e instanceof ParamErrorException){
            ParamErrorException exception=(ParamErrorException)e;
            if(StringUtils.isNotBlank(exception.getCode())){
                out.setCode(exception.getCode()); 
            }else{
                out.setCode(ResultCode.PARAMS_ERROR);
            }
            out.setMessage(exception.getMessage());
            out.setErrorData(exception.getErrorData());
            LOGGER.error("失败:",e);
            return out;
        } 
        if(e instanceof IllegalArgumentException){
            out.setCode(ResultCode.PARAMS_ERROR);
            out.setMessage(e.getMessage());
            LOGGER.error("失败:",e);
            return out;
        } 
        if(e instanceof NotAuthException){
            NotAuthException exception=(NotAuthException)e;
            if(StringUtils.isNotBlank(exception.getCode())){
                out.setCode(exception.getCode()); 
            }else{
                out.setCode(ResultCode.UNAUTHORIZED);
            }
            out.setMessage(exception.getMessage());
            out.setErrorData(exception.getErrorData());
            LOGGER.error("失败:",e);
            return out;
        }
        if(e instanceof DataErrorException){
            DataErrorException exception=(DataErrorException)e;
            if(StringUtils.isNotBlank(exception.getCode())){
                out.setCode(exception.getCode()); 
            }else{
                out.setCode(ResultCode.DATA_ERROR);                
            }
            out.setMessage(e.getMessage());
            out.setErrorData(exception.getErrorData());
            LOGGER.error("失败:",e);
            return out;
        }
        out.setCode(ResultCode.APP_ERROR);

        out.setMessage("系统处理发生异常");
        LOGGER.error("失败:", e);
        return out;
    }


    protected List<String>  getBizData(){
       return Arrays.asList(request.getParameter("bizIds"));
    }

}
