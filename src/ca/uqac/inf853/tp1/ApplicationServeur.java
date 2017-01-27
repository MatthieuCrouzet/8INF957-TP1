package ca.uqac.inf853.tp1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplicationServeur {

	private int m_port;
	private ServerSocket m_server_socket;
	
	/**
	 * prend le numéro de port crée un SocketServer sur le port
	 * @throws IOException 
	 */
	public ApplicationServeur(int port) throws IOException{
		m_port = port;
		m_server_socket = new ServerSocket(m_port);
		
	}
	
	/**
	 * se met en attente de connexions des clients. Suite aux connexions, elle lit
	 * ce qui est envoyé à travers la Socket, recrée l'objet Commande envoyé par
	 * le client, et appellera traiterCommande(Commande uneCommande)
	 * @throws IOException 
	 */
	public void aVosOrdre() throws IOException{
		while(true){
			Socket clientSocket = m_server_socket.accept();
			System.out.println("Connexion entrante");
		
			/* Création des flus d'entrées et de sorties vers le client */
			ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inToClient = new ObjectInputStream(clientSocket.getInputStream());
			
			/* Reception des objets depuis le client */

			try {
				Commande commande = (Commande) inToClient.readObject();
				System.out.println(commande);
				/* Envoi du message au client */
				outToClient.writeObject(commande);
			} catch (ClassNotFoundException e) {
				System.out.println("l'objet envoyé n'est pas de type Commande : " + e.getMessage());
			}
			
			
		}
	}
	/**
	 * prend uneCommande dument formatée et la traite. Dépendant du type de commande,
	 * elle appelle la méthode spécialisée
	 */
	public void traiteCommande(Commande uneCommande){
		
	}
	
	/**
	 * traiterLecture : traite la lecture d'un attribut. Renvoie le résultat par le
	 * socket
	 */
	public void traiterLecture(Object pointeurObjet, String attribut){
		
	}
	
	/**
	 * traiterEcriture : traite l'écriture d'un attribut. Confirme au client que l'écriture
	 * s'est faite correctement.
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur){
		
	}
	
	/**
	 * traiterCreation : traite la création d'un objet. Confirme au client que la création
	 * s'est faite correctement.
	 */
	public void traiterCreation(Class classeDeLobjet, String identificateur){
		
	}
	
	/**
	 * traiterChargement : traite le chargement d'une classe. Confirmes au client que la création
	 * s'est faite correctement.
	 */
	public void traiterChargement(String nomQualifie){
		
	}
	
	/**
	 * traiterCompilation : traite la compilation d'un fichier source java. Confirme au client
	 * que la compilation s'est faite correctement. Le fichier source est donné par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource){
		
	}
	
	/**
	 * traiterAppel : traite l'appel d'une méthode, en prenant comme argument l'objet
	 * sur lequel on effectue l'appel, le nom de la fonction à appeler, un tableau de nom de
	 * type des arguments, et un tableau d'arguments pour la fonction. Le résultat de la
	 * fonction est renvoyé par le serveur au client (ou le message que tout s'est bien
	 * passé)
	 */
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types, Object[] valeurs){
		
	}
	
	/**
	 * programme principale. Prend 4 arguments: 1) numéro de port, 2) répertoire source,
	 * 3) répertoire classes et 4) nom du fichier de traces (sortie)
	 * Cette méthode doit créer une instance de la classe ApplicationServeur, l'initialiser
	 * puis appeler aVosOrdres sur cet objet
	 */
	public static void main(String[] args) {
		if (args.length != 4){
			System.out.println("Erreur, il faut 4 arguments d'entrées sur le programme client");
			System.exit(1);
		}
		int port = Integer.parseInt(args[0]);
		String repertoireSource = args[1];
		String repertoireClasses = args[2];
		String fichSortie = args[3];
		try {
			ApplicationServeur appServeur = new ApplicationServeur(port);
			appServeur.aVosOrdre();
		} catch (IOException e) {
			System.err.println("Server Error: " + e.getMessage());
	        System.err.println("Localized: " + e.getLocalizedMessage());
	        System.err.println("Stack Trace: " + e.getStackTrace());
	        System.err.println("To String: " + e.toString());
		}

	}

}
