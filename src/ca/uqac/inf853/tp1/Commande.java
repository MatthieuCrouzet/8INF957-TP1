package ca.uqac.inf853.tp1;

import java.io.Serializable;

/**
 * 
 * @author Baptiste Buron, Matthieu Crouzet
 * implémente l’interface Serializable. Elle est utilisée pour emmagasiner la description d’une commande,
 * ce sont des instances de cette classe qu’on sérialisera et qu’on enverra à travers les sockets ou RMI.
 *
 */
public class Commande implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 412838999712996492L;
	private String m_commande;
	
	/**
	 * Crée une commande selon un descriptif
	 * @param commande : represente le descriptif de la commande
	 */
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
