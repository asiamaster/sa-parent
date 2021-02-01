package com.sa.metadata.annotation;

import com.sa.metadata.FieldEditor;

import java.lang.annotation.*;



@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditMode {


	boolean required() default false;


	boolean visible() default true;


	boolean readOnly() default false;


	FieldEditor editor() default FieldEditor.Text;


	String params() default "";


	int index() default Integer.MAX_VALUE;


	boolean sortable() default true;

	boolean formable() default true;

	boolean gridable() default true;

	boolean queryable() default true;




	String provider() default "";


	String txtField() default "";

}
