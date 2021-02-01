package com.sa.mvc.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;


public class BeanValidator {

    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static String validator(Object bean, Class<?>... groups) {
        StringBuffer buf = new StringBuffer();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean,groups);
        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
            buf.append(constraintViolation.getMessage()).append("; ");
        }
        return buf.toString();
    }
}