package main.java.messages;

import java.io.Serializable;

public class ResultMessage implements Serializable {
	private String tqbfId;
	private boolean result;

	public String getTqbfId() {
		return tqbfId;
	}

	public void setTqbfId(String tqbfId) {
		this.tqbfId = tqbfId;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
