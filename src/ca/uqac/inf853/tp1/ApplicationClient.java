package ca.uqac.inf853.tp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Baptiste Buron, Matthieu Crouzet
 * cette classe gère la partie client.
 * peut demander au serveur de crée des objets de type Commande à partir d'un fichier d'entrée
 * récupère les résultats et les imprime dans un fichier de sortie
 */
public class ApplicationClient {

	private PrintWriter sortieWriter;
	private BufferedReader commandesReader;
	private String m_host;
	private int m_port;

	/**
	 * Initialise l'adresse et le port du socket client
	 * @param host : adresse du socket client
	 * @param port : port du socket client
	 */
	public ApplicationClient(String host, int port){
		this.m_host = host;
		this.m_port = port;
	}
	
	/**
	 * prend le fichier contenant la liste des commandes et le charge dans une
	 * variable du type Commande qui est retournée
	 * @param fichier : le fichier devant être lu contenant les commandes à exécuter
	 * @throws IOException 
	 */
	public Commande saisisCommande(BufferedReader fichier) throws IOException{
		String commande = null;
		if ((commande = fichier.readLine()) != null) {
			System.out.println(commande);
			return new Commande(commande);
		}
		return null;

	}

	/**
	 * initialise : ouvre les différents fichiers de lecture et écriture
	 * @param : fichCommandes : Le nom du fichier contenant les commandes à exécuter
	 * @param : fichSorties: Le nom du fichier devant contenir les résultats à la fin de la connexion client-serveur
	 * @throws IOException 
	 */
	public void initialise(String fichCommandes, String fichSortie) throws IOException{
		//ouverture fichier de lecture
		FileReader fichLecture = new FileReader(fichCommandes);
		commandesReader = new BufferedReader(fichLecture);

		//ouverture fichier de sortie
		FileWriter fichEcriture = new FileWriter(fichSortie);
		BufferedWriter brWriter = new BufferedWriter(fichEcriture);
		sortieWriter = new PrintWriter(brWriter);
	}

	/**
	 * prend une Commande dûment formatée, et la fait exécuter par le serveur. Le résultat de
	 * l'execution est retournée. Si la commande ne retourne pas de résultat, on retourne null.
	 * Chaque appel doit ouvrir une connexion, exécuter et fermer la connexion.
	 * uneCommande : l'objet de la classe Commande devant être exécuter
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClassNotFoundException 
	 */
	public Object traiteCommande(Commande uneCommande) throws UnknownHostException, IOException, ClassNotFoundException{;
		Socket socket = new Socket(m_host, m_port);

		/* Création des flus d'entrées et de sorties vers le serveur */
		ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());

		/* Envoi du message au serveur */
		outToServer.writeObject(uneCommande);

		/* Reception des objets depuis le serveur */
		Object commandeExecutee = inFromServer.readObject();
		socket.close();

		return commandeExecutee;
	}

	/**
	 * indique la séquence d'étapes à exécuter pour le test. 
	 * elle fait des appels successifs à saisisCommande(BufferedReader fichier) et
	 * traiteCommande(Commande uneCommande)
	 * @throws IOException 
	 */
	public void scenario() throws IOException{
		sortieWriter.println("Debut des traitements:");
		Commande prochaine = saisisCommande(commandesReader);
		while (prochaine != null) {
			sortieWriter.println("\tTraitement de la commande " + prochaine + " ...");
			Object resultat;
			try {
				resultat = traiteCommande(prochaine);
				sortieWriter.println("\t\tResultat: " + resultat);
			} catch (ClassNotFoundException e) {
				System.err.println("Client Error: " + e.getMessage());
				System.err.println("Localized: " + e.getLocalizedMessage());
				System.err.println("Stack Trace: " + e.getStackTrace());
			}
			prochaine = saisisCommande(commandesReader);
		}
		sortieWriter.println("Fin des traitements");
	}

	/**
	 * programme principal. Prend 4 arguments: 1)"hostname" du serveur, 2) numéro de port,
	 * 3) nom fichier commandes, et 4) nom fichier sortie. Cette méthode crée une
	 * instance de la classe Application Client, l'initialise, puis exécute le scénario
	 */
	public static void main(String[] args){

		if (args.length != 4){
			System.out.println("Erreur, il faut 4 arguments d'entrées sur le programme client");
			System.exit(1);
		}
		String host =  args[0];
		int port = Integer.parseInt(args[1]);
		String fichEntree = args[2];
		String fichSortie = args[3];
		ApplicationClient appClient = new ApplicationClient(host, port);

		try {
			appClient.initialise(fichEntree, fichSortie);
			appClient.scenario();
			appClient.commandesReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		appClient.sortieWriter.close();
	}
}
