package net.cattweasel.pokebot.tools;

public class GeneralException extends Exception {

	private static final long serialVersionUID = -1010847125800122615L;

	public GeneralException(Exception ex) {
		super(ex);
	}
	
	public GeneralException(String msg) {
		super(msg);
	}
	
	public GeneralException(Throwable t) {
		super(t);
	}
}
