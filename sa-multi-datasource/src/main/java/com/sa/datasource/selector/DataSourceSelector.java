package com.sa.datasource.selector;



public abstract class DataSourceSelector {


	public abstract String fetch(boolean writable);


	public abstract String fetchDefault();

}
