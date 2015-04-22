package com.uumind.log4j.appender.redis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class H2Test {

	public static void main(String[] args) throws Exception {
		Class.forName("org.h2.Driver");
        Connection con = DriverManager.getConnection("jdbc:h2:~/logs", "logs", "logs");
        Statement stmt = con.createStatement();
        stmt.executeUpdate( "CREATE TABLE IF NOT EXISTS table1 ( user varchar(50) )" );
        stmt.executeUpdate( "INSERT INTO table1 ( user ) VALUES ( 'Claudio' )" );
        stmt.executeUpdate( "INSERT INTO table1 ( user ) VALUES ( 'Bernasconi' )" );

        ResultSet rs = stmt.executeQuery("SELECT * FROM table1");
        while( rs.next() )
        {
            String name = rs.getString("user");
            System.out.println( name );
        }
        stmt.close();
        con.close();
	}

}
