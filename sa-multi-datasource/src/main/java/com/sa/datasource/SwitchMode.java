package com.sa.datasource;


public enum SwitchMode {
	MULTI("1"), MASTER_SLAVE("2");

	private String code;

	SwitchMode(String code){
		this.code = code;
	}

	public String getCode() {
		return code;
	}


	public static SwitchMode getSwitchModeByCode(String code){
		for(SwitchMode switchMode : SwitchMode.values()){
			if(switchMode.getCode().equals(code)){
				return switchMode;
			}
		}
		return null;
	}
}
