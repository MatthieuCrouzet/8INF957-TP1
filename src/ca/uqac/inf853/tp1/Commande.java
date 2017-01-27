package ca.uqac.inf853.tp1;

import java.io.Serializable;
import java.util.List;

public class Commande implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 412838999712996492L;
	private String m_commande;
	
	public Commande(String commande){
		m_commande = commande;
	}
	
	public void traiterCommande(){
		
	}
	
	public String toString(){
		String ret = "";
		ret = "Commande : " + m_commande;
		return ret;
	}
	
}
