package com.sa.beetl;


public class TagStyle {
	String[] attrs = null;
	public TagStyle(String style){
		if(style==null||style.length()==0){
			attrs = new String[0];
			
		}else{
			attrs = style.split(" ");
		}		
	}
	

	
	private int find(String key){
		for(int i=0;i<attrs.length;i++){
			 if(attrs[i].equals(key)){
				 return i;
			 }
		}
		return -1;
	}
	
	private int find(char key){
		for(int i=0;i<attrs.length;i++){
			 if(attrs[i].charAt(0)==key){
				 if(attrs[i].charAt(1)>='0'&&attrs[i].charAt(1)<='9'){
					 return i;
				 }
			 }
		}
		return -1;
	}
}
