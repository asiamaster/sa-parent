package com.sa.datasource;


public enum SelectorMode {
	ROUND_ROBIN("1"), WEIGHTED_ROUND_ROBIN("2");

	private String code;

	SelectorMode(String code){
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	
	public static SelectorMode getSelectorModeByCode(String code){
		for(SelectorMode switchMode : SelectorMode.values()){
			if(switchMode.getCode().equals(code)){
				return switchMode;
			}
		}
		return null;
	}
}
