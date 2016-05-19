package com.finalproject.smartspeechseparator.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import com.finalproject.smartspeechseparator.audioprocessing.WavFile;
import com.finalproject.smartspeechseparator.general.SystemSettings;
import com.mysql.jdbc.EscapeTokenizer;
import com.mysql.jdbc.PreparedStatement;

public class DB_handler {

	private Connection conn;

	private String dbName = "smartsoundseparationdb";
	private String soundTableName = "sound", separatedTableName = "separated", stf_wlenTableName = "stft_wlen",
			basisTableName = "basis";

	private String ServerID = "jdbc:mysql://localhost:3306/" + dbName, UserName = "root", Password = "root";

	public Connection getConnection() {
		return conn;
	}

	public DB_handler() {
		this.conn = ConnectToDB();
	}

	public Connection ConnectToDB() {
		try {
			return DriverManager.getConnection(ServerID, UserName, Password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getSoundTableName() {
		return soundTableName;
	}

	public void setSoundTableName(String soundTableName) {
		this.soundTableName = soundTableName;
	}

	public String getSeparatedTableName() {
		return separatedTableName;
	}

	public void setSeparatedTableName(String separatedTableName) {
		this.separatedTableName = separatedTableName;
	}

	public String getStf_wlenTableName() {
		return stf_wlenTableName;
	}

	public void setStf_wlenTableName(String stf_wlenTableName) {
		this.stf_wlenTableName = stf_wlenTableName;
	}

	public String getBasisTableName() {
		return basisTableName;
	}

	public void setBasisTableName(String basisTableName) {
		this.basisTableName = basisTableName;
	}

	public String getServerID() {
		return ServerID;
	}

	public void setServerID(String serverID) {
		ServerID = serverID;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public void switchAutoCommit(boolean state) throws SQLException {
		conn.setAutoCommit(state);
	}

	public void commit() throws SQLException {
		conn.commit();
	}

	public static void main(String[] args) {

		String filepath = "E:" + File.separator + "MATLAB_WORK" + File.separator + "Project" + File.separator + "data"
				+ File.separator + "mixed_sound.wav";
		DB_handler db = new DB_handler();

		for (String dirpath : SystemSettings.systemClusters) {
			Path path = Paths.get(dirpath);
			if (!Files.exists(path))
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		// Sound_row sr = db.copyFileTodatabase(filepath,
		// SystemSettings.sound_speechDirPath);

		// System.out.println(sr.getDir() + "\n" + sr.getName());

		try {
			// db.DeleteSound("mixed_sound.wav");
			// db.InsertSound(filepath, SOUNDTYPE.speech);
			List<Sound_row> li = db.selectSounds("select * from smartsoundseparationdb.sound;");
			for (Sound_row sr : li) {
				System.out.println(sr.getId() + ",\t" + sr.getDir() + ",\t" + sr.getName() + ",\t" + sr.getType()
						+ ",\t" + sr.isIs_learned());
			}

			li = db.getAllSoundsOfType(SOUNDTYPE.mix, false);
			System.out.println("================================================================");
			for (Sound_row sr : li) {
				System.out.println(sr.getId() + ",\t" + sr.getDir() + ",\t" + sr.getName() + ",\t" + sr.getType()
						+ ",\t" + sr.isIs_learned());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		db.close();
	}

	public String copySpectrogramToDatabase(String imgPath, SOUNDTYPE type) throws Exception {
		String goal = "";
		switch (type) {
		case speech:
			goal = SystemSettings.spectrum_speechDirPath;
			break;
		case noise:
			goal = SystemSettings.spectrum_noiseDirPath;

			break;
		case eq:
			goal = SystemSettings.spectrum_eqDirPath;

			break;
		case sep_speech:
			goal = SystemSettings.spectrum_sep_speechDirPath;

			break;
		case sep_noise:
			goal = SystemSettings.spectrum_sep_noiseDirPath;

			break;
		case mix:
			goal = SystemSettings.spectrum_mixDirPath;

			break;
		default:
			throw new Exception("copySpectrogramToDatabase  Type is not applicable!!");

		}

		String name = getFileName(imgPath);

		goal += name;
		Files.copy(Paths.get(imgPath), Paths.get(goal), StandardCopyOption.REPLACE_EXISTING);

		return goal;
	}

	public String getFileName(String path) {
		int ptr = path.length() - 1;
		char last = path.charAt(ptr);
		String filename = "";
		while ((last != '\\') && (last != '/') && ptr >= 0) {
			last = path.charAt(ptr);
			filename = last + filename;
			ptr--;
		}

		return filename;
	}

	public Sound_row copyFileTodatabase(String wavFilePath, String goalPath) {
		Sound_row output = new Sound_row();

		try {

			output.setDir(goalPath);

			// String[] spp = wavFilePath.split(File.separator+File.separator);
			File file = new File(wavFilePath);
			String filename = file.getName();//getFileName(wavFilePath);

			output.setName(filename);
			goalPath += filename;

			WavFile wavfile = WavFile.openWavFile(new File(wavFilePath));

			double[][] data = new double[wavfile.getNumChannels()][(int) wavfile.getNumFrames()];

			wavfile.readFrames(data, (int) wavfile.getNumFrames());

			double[][] dataTocopy = new double[1][(int) wavfile.getNumFrames()];

			for (int i = 0; i < dataTocopy[0].length; i++) {
				dataTocopy[0][i] = data[0][i];
			}
			WavFile goalWavfile = WavFile.newWavFile(new File(goalPath), 1, dataTocopy[0].length,
					wavfile.getValidBits(), wavfile.getSampleRate());

			goalWavfile.writeFrames(dataTocopy, dataTocopy[0].length);
			wavfile.close();
			goalWavfile.close();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return output;

	}

	public boolean InsertSound(String wav_path, SOUNDTYPE type, Sound_row record_out) throws Exception {

		if (!wav_path.substring(wav_path.length() - 4, wav_path.length()).equals(".wav")) {
			throw new Error("ERROR: the file  \'" + wav_path + "\'  is not .wav file!");
		}

		Statement stmt = conn.createStatement();

		// PreparedStatement updatestmt = null;
		Sound_row row = new Sound_row();
		switch (type) {
		case speech:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_speechDirPath);
			break;
		case noise:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_noiseDirPath);

			break;
		case eq:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_eqDirPath);

			break;
		case sep_speech:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_sep_speechDirPath);

			break;
		case sep_noise:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_sep_noiseDirPath);

			break;
		case mix:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_mixDirPath);

			break;
		default:
			throw new Exception("DB_handler.InsertSound Error Type is not aplicable!");

		}
		// conn.setAutoCommit(false);
		row.setType(type.toString());

		String query = "Insert into " + dbName + "." + soundTableName + " (dir,name,type) VALUES ( \'"
				+ escapeBackslash(row.getDir()) + "\', \'" + row.getName() + "\',\'" + row.getType() + "\' );";

		// String pquery = "Insert into " + dbName + "." + soundTableName + "
		// (dir,name,type) VALUES (?,?,?);";
		// updatestmt = (PreparedStatement) conn.prepareStatement(pquery);
		// updatestmt.setNString(1, row.getDir());
		// updatestmt.setNString(2, row.getName());
		// updatestmt.setString(3, row.getType());
		// updatestmt.execute();
		// conn.commit();
		// System.out.println(query);
		try{
			stmt.executeUpdate(query);
		}catch(Exception e){
			e.printStackTrace();
		}
		conn.commit();
		Sound_row selected = selectSoundByName(row.getName());
		record_out.setId(selected.getId());
		record_out.setDir(selected.getDir());
		record_out.setName(selected.getName());
		record_out.setIs_learned(selected.isIs_learned());
		record_out.setType(selected.getType());
		record_out.setSpectrogramPath(selected.getSpectrogramPath());
		record_out.setDescription(selected.getDescription());

		return true;

	}

	public Sound_row selectSoundByName(String name) throws Exception {
		List<Sound_row> li = selectSounds("select * from " + dbName + "." + soundTableName + " where name = \'" + name + "\';");
		return li.get(0);
	}

	public boolean InsertSound(String wav_path, SOUNDTYPE type) throws Exception {

		if (!wav_path.substring(wav_path.length() - 4, wav_path.length()).equals(".wav")) {
			throw new Error("ERROR: the file  \'" + wav_path + "\'  is not .wav file!");
		}

		Statement stmt = conn.createStatement();

		// PreparedStatement updatestmt = null;
		Sound_row row = new Sound_row();
		switch (type) {
		case speech:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_speechDirPath);
			break;
		case noise:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_noiseDirPath);

			break;
		case eq:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_eqDirPath);

			break;
		case sep_speech:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_sep_speechDirPath);

			break;
		case sep_noise:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_sep_noiseDirPath);

			break;
		case mix:
			row = copyFileTodatabase(wav_path, SystemSettings.sound_mixDirPath);

			break;
		default:
			return false;

		}
		// conn.setAutoCommit(false);
		row.setType(type.toString());
		String query = "Insert into " + dbName + "." + soundTableName + " (dir,name,type) VALUES ( \'"
				+ escapeBackslash(row.getDir()) + "\', \'" + row.getName() + "\',\'" + row.getType() + "\' );";

		// String pquery = "Insert into " + dbName + "." + soundTableName + "
		// (dir,name,type) VALUES (?,?,?);";
		// updatestmt = (PreparedStatement) conn.prepareStatement(pquery);
		// updatestmt.setNString(1, row.getDir());
		// updatestmt.setNString(2, row.getName());
		// updatestmt.setString(3, row.getType());
		// updatestmt.execute();
		// conn.commit();
		// System.out.println(query);
		stmt.executeUpdate(query);
		return true;

	}

	public String escapeBackslash(String s) {
		return s.replace("\\", "\\\\");
	}

	public boolean InsertBasis(Sound_row parentSound, StringBuilder B, int stft_wlen, int K) throws Exception {

		Basis_row row = new Basis_row();

		row.setData(B);
		row.setK(K);
		row.setSound_id(parentSound.getId());
		row.setStft_win_len(stft_wlen);

		List<Basis_row> basisList = getBasisBySoundName(parentSound.getName());
		for (Basis_row br : basisList) {
			if (br.getK() == K && br.getStft_win_len() == stft_wlen) {
				throw new Exception("Basis for \'" + parentSound.getName()
						+ "\' Already exist in database! with the same settings!!");
			}
		}

		Statement stmt = conn.createStatement();

		String query = "Insert into " + dbName + "." + stf_wlenTableName + " values ( \'" + stft_wlen + "\' );";
		String stfQuery = "select * from " + dbName + "." + stf_wlenTableName + " where stft_wlen = \'" + stft_wlen
				+ "\' ;";
		ResultSet result = stmt.executeQuery(stfQuery);

		if (!result.next()) {
			try {

				stmt.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		query = "insert into " + dbName + "." + basisTableName + " ( k, s_id, data, stft_win_len ) values (\'" + K
				+ "\', \'" + row.getSound_id() + "\', \'" + row.getData().toString() + "\', \'" + stft_wlen + "\');";

		int ret = stmt.executeUpdate(query);

		if (ret <= 0) {
			throw new Exception("InsertBasis Faild! cann't insert  " + parentSound.getName());
		}
		System.out.println("Basis B inserted with sound \'" + parentSound.getName() + "\'");
		return true;
	}

	public List<Integer> getAllSTFT_Wlen() throws Exception{
		Statement stmt = conn.createStatement();
		
		ResultSet result = stmt.executeQuery("select * from "+dbName+"."+stf_wlenTableName+";");
		List<Integer> out = new ArrayList<>();
		while(result.next()){
			out.add(result.getInt(1));
		}
		
		return out;
		
	}
	
	public List<Basis_row> getBasis(String sql) throws SQLException{
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		List<Basis_row> out = new ArrayList<>();
		while(result.next()){
			Basis_row row = new Basis_row();
			
			row.setId(result.getInt(1));
			row.setSound_id(result.getInt(3));
			row.setData(new StringBuilder(result.getNString(4)));
			row.setK(result.getInt(2));
			row.setStft_win_len(result.getInt(5));
			
			out.add(row);
		}
		
		if(out.size() == 0)
			return null;
		
		return out;
	}
	public boolean DeleteSound(String name) throws Exception {
		Statement stmt = conn.createStatement();

		ResultSet result = stmt
				.executeQuery("select * from " + dbName + "." + soundTableName + " where name = \'" + name + "\';");
		if (result.next()) {
			System.out.println("Deleting Record!");
			Sound_row sr = new Sound_row();

			sr.setId(result.getInt("id"));
			sr.setDir(result.getNString("dir"));
			sr.setIs_learned(result.getBoolean("is_learned"));
			sr.setName(result.getNString("name"));
			sr.setDescription(result.getNString("description"));
			sr.setType(result.getNString("type"));

			Files.deleteIfExists(Paths.get(escapeBackslash(sr.getDir()) + sr.getName()));

			stmt.executeUpdate(
					"delete from " + dbName + "." + basisTableName + " where s_id = \'" + sr.getId() + "\' ;");
			/// stmt.executeUpdate("delete from " + dbName + "." +
			/// separatedTableName+" where s_id = \'"+sr.getId()+"\' ;" );

			stmt.executeUpdate("delete from " + dbName + "." + soundTableName + " where name = " + "\'" + name + "\';");

			System.out.println("Record deleted!   " + name);
		} else {
			System.out.println("Record not Exist!  " + name);
			return false;
		}

		return true;
	}

	public Sound_row getRecordByName(String soundname) {
		List<Sound_row> li;
		try {
			li = selectSounds(
					"select * from " + dbName + "." + soundTableName + " where name = \'" + soundname + "\';");

			if (li.isEmpty())
				return null;
			else
				return li.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<Sound_row> selectSounds(String selectStmt) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(selectStmt);

		List<Sound_row> select_res = new ArrayList<Sound_row>();

		while (result.next()) {

			Sound_row sr = new Sound_row();

			//System.out.println("ID ==== "+result.getInt(1)+"\nDIR ==== "+result.getNString(2)+"\nNAME ==== "+result.getNString(3));
			sr.setId(result.getInt("id"));
			sr.setDir(result.getNString("dir"));
			sr.setIs_learned(result.getBoolean("is_learned"));
			sr.setName(result.getNString("name"));
			sr.setDescription(result.getNString("description"));
			sr.setType(result.getNString("type"));
			sr.setSpectrogramPath(result.getNString("spectrogram_path"));

			select_res.add(sr);
		}
		//System.out.println("ID ==== "+select_res.get(0).getId()+"\nDIR ==== "+select_res.get(0).getDir()+"\nNAME ==== "+select_res.get(0).getName());	
		return select_res;
	}

	public String[] insertSoundSet(List<String> sounds, SOUNDTYPE type) {
		String[] out = new String[sounds.size()];

		for (int s = 0; s < out.length; s++) {
			try {
				this.InsertSound(sounds.get(s), type);
				out[s] = sounds.get(s) + "\t success!";
			} catch (Exception e) {
				out[s] = sounds.get(s) + "\t fail!";
				e.printStackTrace();
			}
		}
		return out;
	}

	public List<Sound_row> getAllSoundsOfType(SOUNDTYPE type, boolean learned) {
		List<Sound_row> out = new ArrayList<>();
		try {
			out = null;
			out = selectSounds("select * from " + dbName + "." + soundTableName + " where type = \'" + type.toString()
					+ "\' and is_learned = \'" + learned + "\';");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	public Basis_row getLastBasie(SOUNDTYPE type, int stft_wlen, int k) {

		try {

			List<Basis_row> li = getAllBasisOfType(type);
			Basis_row outelement = new Basis_row(-1, -1, k, stft_wlen, null);
			if (li.size() > 0) {
				for (Basis_row br : li) {
					if (br.getK() == outelement.getK() && br.getStft_win_len() == outelement.getStft_win_len()
							&& br.getId() > outelement.getId())
						outelement = new Basis_row(br.getId(), br.getSound_id(), br.getK(), br.getStft_win_len(),
								br.getData());
				}

				if (outelement.getId() == -1)
					return null;

				return outelement;

			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public int UpdateSoundRowByID(int ID, boolean is_learned) throws Exception {
		return UpdateSoundTable("update " + dbName + "." + soundTableName + " set is_learned = \'" +( is_learned == true ? 1:0)
				+ "\' where id = \'" + ID + "\';");
	}

	public int UpdateSoundRowByName(String name, String spectrogramPath, SOUNDTYPE type, boolean deletOregenal) throws Exception {
		String spPath = copySpectrogramToDatabase(spectrogramPath, type);
		if(deletOregenal)
			Files.delete(Paths.get(spectrogramPath));
		
		return UpdateSoundTable("update " + dbName + "." + soundTableName + " set spectrogram_path = \'" + spPath
				+ "\' where name = \'" + name + "\';");
	}

	public int UpdateSoundRowByName(String name, boolean is_learned) throws Exception {
		return UpdateSoundTable("update " + dbName + "." + soundTableName + " set is_learned = \'" + is_learned
				+ "\' where name = \'" + name + "\';");
	}

	public int UpdateSoundTable(String sql) throws Exception {

		Statement stmt = conn.createStatement();
		return (int) stmt.executeLargeUpdate(sql);
	}

	public List<Sound_row> getSoundsWithType_NotK_wlen(SOUNDTYPE type, int not_equal_stft_wlen, int not_equal_K) {
		List<Sound_row> out = new ArrayList<>();
		try {
			out = null;
			out = getAllSoundsOfType(type, false);
			List<Sound_row> learnedLI = getAllSoundsOfType(type, true);
			List<Basis_row> basis = selectBasis("select * from " + dbName + "." + basisTableName
					+ " where stft_win_len = \'" + not_equal_stft_wlen + "\' and k = \'" + not_equal_K + "\' ;");

			for (Sound_row sr : learnedLI) {
				boolean good = true;
				for (Basis_row br : basis) {
					if (br.getSound_id() == sr.getId()) {
						good = false;
						break;
					}
				}
				if (good)
					out.add(sr);
			}

		} catch (Exception e) {
			out = null;
			e.printStackTrace();
		}

		return out;
	}

	public List<Basis_row> selectBasis(String selectStmt) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(selectStmt);

		List<Basis_row> select_res = new ArrayList<Basis_row>();

		while (result.next()) {

			Basis_row br = new Basis_row();

			br.setId(result.getInt("id"));
			br.setK(result.getInt("k"));
			br.setSound_id(result.getInt("s_id"));
			br.setStft_win_len(result.getInt("stft_win_len"));
			br.setData(new StringBuilder(result.getNString("data")));

			select_res.add(br);
		}

		return select_res;
	}

	public List<Basis_row> getBasisBySoundName(String soundname) throws Exception {

		Sound_row s_row = getRecordByName(soundname);
		if (s_row == null) {
			throw new Exception("Error getRecordByName record not found!");
		}

		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(
				"select * from " + dbName + "." + basisTableName + " where s_id = \'" + s_row.getId() + "\';");
		List<Basis_row> output = new ArrayList<>();
		while (result.next()) {
			Basis_row row = new Basis_row();

			row.setId(result.getInt("id"));
			row.setK(result.getInt("k"));
			row.setSound_id(result.getInt("s_id"));
			row.setStft_win_len(result.getInt("stft_win_len"));
			row.setData(new StringBuilder(result.getNString("data")));

			output.add(row);
		}

		return output;
	}

	public void clearDB() throws Exception {
		clearTable(soundTableName);
		clearTable(basisTableName);
		clearTable(separatedTableName);
		clearTable(stf_wlenTableName);
	}

	public void clearTable(String tableName) throws Exception {
		Statement stmt = conn.createStatement();
		int affected_rows = -1;
		String col = "id";
		if (tableName.equals(stf_wlenTableName))
			col = "stft_wlen";
		affected_rows = stmt.executeUpdate("delete from " + dbName + "." + tableName + " where " + col + " <> '-1';");
		System.out.println(tableName + " table Cleared!  # " + affected_rows + "  rows affected!");
	}

	public List<Basis_row> getAllBasisOfType(SOUNDTYPE type) throws Exception {
		return selectBasis("select * from " + dbName + "." + basisTableName + " join " + dbName + "." + soundTableName
				+ " on ( " + soundTableName + "." + "id = " + basisTableName + "." + "s_id )  where type = \'"
				+ type.toString() + "\';");
	}
	
	public List<Basis_row> getAllBasisOf(SOUNDTYPE type, int stft_wlen) throws Exception {
		return selectBasis("select * from " + dbName + "." + basisTableName + " join " + dbName + "." + soundTableName
				+ " on ( " + soundTableName + "." + "id = " + basisTableName + "." + "s_id )  where type = \'"
				+ type.toString() + "\' and stft_win_len = \'"+stft_wlen+"\' ;");
	}

	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
