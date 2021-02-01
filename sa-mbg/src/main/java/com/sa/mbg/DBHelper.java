package com.sa.mbg;


import org.mybatis.generator.api.ConnectionFactory;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.JDBCConnectionFactory;
import org.mybatis.generator.internal.ObjectFactory;

import java.sql.Connection;
import java.sql.SQLException;


public class DBHelper {

	private static Connection conn = null;


	public static Connection getConnection(Context context) {
		try {
			if(conn == null) {
				ConnectionFactory connectionFactory;
				if (context.getJdbcConnectionConfiguration() != null) {
					connectionFactory = new JDBCConnectionFactory(context.getJdbcConnectionConfiguration());
				} else {
					connectionFactory = ObjectFactory.createConnectionFactory(context);
				}
				return connectionFactory.getConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
