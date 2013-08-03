package org.dbist.dml;

import org.dbist.annotation.Sequence;

public class Log {
	@Sequence(name = "SQ_LOG")
	private Long id;
	private String text;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
