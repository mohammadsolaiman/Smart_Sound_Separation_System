package com.finalproject.smartspeechseparator.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class TestDB {

	public static void main(String[] args) {

		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartsoundseparationdb", "root", "root");
			
			Statement stmt = conn.createStatement();
			ResultSet result =  stmt.executeQuery("select * from smartsoundseparationdb.sound;");
			
			List<Sound_row> select_res = new ArrayList<Sound_row>();
			
			while(result.next()){
				
				Sound_row sr = new Sound_row();
				
				sr.setId(result.getInt("id"));
				sr.setDir(result.getNString("dir"));
				sr.setIs_learned(result.getBoolean("is_learned"));
				sr.setName(result.getNString("name"));
				
				
				select_res.add(sr);
			}
			System.out.println(select_res.get(0).getId()+"\t"+select_res.get(0).getDir());
			
			System.out.println("done!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
