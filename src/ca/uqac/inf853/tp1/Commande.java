package ca.uqac.inf853.tp1;

import java.io.Serializable;

public class Commande implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 412838999712996492L;
	private String m_commande;
	
	public Commande(String commande){
		m_commande = commande;
	}
	
	public String getCommandeDescription(){
		return m_commande;
	}
	
	public String toString(){
		String ret = "";
		ret = "Commande : " + m_commande;
		return ret;
	}
	
}
