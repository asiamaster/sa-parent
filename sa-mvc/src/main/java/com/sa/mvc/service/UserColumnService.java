package com.sa.mvc.service;

import com.sa.mvc.domain.UserColumn;


public interface UserColumnService {


	void saveUserColumns(UserColumn userColumn);


	String[] getUserColumns(UserColumn userColumn);
}
