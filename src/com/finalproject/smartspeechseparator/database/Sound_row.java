package com.finalproject.smartspeechseparator.database;

public class Sound_row {

	private int id;
	private String dir, name, type, description, spectrogramPath;
	private boolean is_learned;

	public Sound_row(int id, String dir, String name, String type, String description, boolean is_learned) {
		this.id = id;
		this.dir = dir;
		this.name = name;
		this.type = type;
		this.description = description;
		this.is_learned = is_learned;
	}

	public Sound_row() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isIs_learned() {
		return is_learned;
	}

	public void setIs_learned(boolean is_learned) {
		this.is_learned = is_learned;
	}

	public String getSpectrogramPath() {
		return spectrogramPath;
	}

	public void setSpectrogramPath(String spectrogramPath) {
		this.spectrogramPath = spectrogramPath;
	}

	
}
