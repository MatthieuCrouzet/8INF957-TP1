package ca.uqac.inf853.tp1;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.tools.*;

import ca.uqac.registraire.Cours;
import ca.uqac.registraire.Etudiant;

public class ApplicationServeur {

	private int m_port;
	private ServerSocket m_server_socket;
	private String m_repertoireSource;
	private String m_repertoireClasses;
	private Object m_sendBackMessage;
	private HashMap<String, Object> m_objectsCreated = new HashMap<>();
	private URLClassLoader m_classLoader;
	private ArrayList<Class<?>> m_classesLoaded;
	
	/**
	 * prend le numéro de port crée un SocketServer sur le port
	 * @param repertoireClasses 
	 * @param repertoireSource 
	 * @throws IOException 
	 */
	public ApplicationServeur(int port, String repertoireSource, String repertoireClasses) throws IOException{
		m_port = port;
		m_server_socket = new ServerSocket(m_port);
		m_repertoireSource = repertoireSource;
		m_repertoireClasses = repertoireClasses;
		m_classLoader = new URLClassLoader(new URL[] { new File(repertoireClasses).toURI().toURL() });
		m_classesLoaded = new ArrayList<Class<?>>();
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
				traiteCommande(commande);
				/* Envoi du message au client */
				outToClient.writeObject(m_sendBackMessage);
				m_sendBackMessage = null;
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
		String[] detailCommande = uneCommande.getCommandeDescription().split("#");
		switch(detailCommande[0]){
			case "compilation":
				traiterCompilation(detailCommande[1]);
				break;
			case "chargement":
				traiterChargement(detailCommande[1]);
				break;
			case "creation":
				try {
					Class<?> classeDeLobjet = Class.forName(detailCommande[1]);
					traiterCreation(classeDeLobjet, detailCommande[2]);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "lecture":
				Object pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				traiterLecture(pointeurObjet, detailCommande[2]);
				break;
			case "ecriture":
				pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				traiterEcriture(pointeurObjet, detailCommande[2], detailCommande[3]);
				break;
				
			case "fonction":
			
				pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				String[] types = null;
				Object[] valeurs = null;
				if(detailCommande.length>4){
					String[] args = detailCommande[3].split(",");
					types = new String[args.length];
					valeurs = new Object[args.length];
					for(int index=0; index < args.length; index++){
						String[] keyValue = args[index].split(":");
						types[index] = keyValue[0];
						valeurs[index] = keyValue[1];
					}
				}
				
				traiterAppel(pointeurObjet, detailCommande[2], types, valeurs);
				
				break;
		}
		
	}
	
	/**
	 * traiterLecture : traite la lecture d'un attribut. Renvoie le résultat par le
	 * socket
	 */
	public void traiterLecture(Object pointeurObjet, String attribut){
		Object res = null;		
		Class<?>[] classTypes = null;
		try {
			Method method = pointeurObjet.getClass().getMethod("get" + attribut.substring(0, 1).toUpperCase() + attribut.substring(1), classTypes);
			res = method.invoke(pointeurObjet);
		} catch (Exception e)  {
			System.out.println(e.getMessage());
			m_sendBackMessage = "Error : " + e.getMessage();
		} 
		if (res != null){
			m_sendBackMessage = "Success : " + res.toString();
		}
	}
	
	/**
	 * traiterEcriture : traite l'écriture d'un attribut. Confirme au client que l'écriture
	 * s'est faite correctement.
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur){
		Object res = null;
		try {
			Method method = pointeurObjet.getClass().getMethod("set" + attribut.substring(0, 1).toUpperCase() + attribut.substring(1), valeur.getClass());
			res = method.invoke(pointeurObjet, valeur);
		} catch (Exception e)  {
			System.out.println(e.getMessage());
			m_sendBackMessage = "Error : " + e.getMessage();
		} 
		if (res != null){
			m_sendBackMessage = "Success : " + res.toString();
		}
	}
	
	/**
	 * traiterCreation : traite la création d'un objet. Confirme au client que la création
	 * s'est faite correctement.
	 */
	public void traiterCreation(Class classeDeLobjet, String identificateur){
		Object res = null;		
		try {
			res = classeDeLobjet.newInstance();
		} catch (Exception e)  {
			System.out.println(e.getMessage());
			m_sendBackMessage = "Error : " + e.getMessage();
		} 
		if (res != null){
			m_objectsCreated.put(identificateur, res);
			m_sendBackMessage = "Success : " + res.toString();
		}
	}
	
	/**
	 * traiterChargement : traite le chargement d'une classe. Confirmes au client que la création
	 * s'est faite correctement.
	 */
	public void traiterChargement(String nomQualifie){
		Class classe = null;
		try {
            classe = m_classLoader.loadClass(nomQualifie);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
			m_sendBackMessage = "Error : " + e.getMessage();
        }
		if (classe != null){
            m_classesLoaded.add(classe);
            m_sendBackMessage = "Success : " + classe;
		}
	}
	
	/**
	 * traiterCompilation : traite la compilation d'un fichier source java. Confirme au client
	 * que la compilation s'est faite correctement. Le fichier source est donné par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource){
		String[] sources = cheminRelatifFichierSource.split(",");    	
    	try {
    		for(int i = 0; i < sources.length; i++){
	    		String command = "javac " + m_repertoireSource + sources[i].substring(5); 
	    		//System.out.println(command);
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
			}
		} catch (Exception e) {
			m_sendBackMessage = "Error : " + e.getMessage();
		}
	}
	
	/**
	 * traiterAppel : traite l'appel d'une méthode, en prenant comme argument l'objet
	 * sur lequel on effectue l'appel, le nom de la fonction à appeler, un tableau de nom de
	 * type des arguments, et un tableau d'arguments pour la fonction. Le résultat de la
	 * fonction est renvoyé par le serveur au client (ou le message que tout s'est bien
	 * passé)
	 */
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types, Object[] valeurs){
		Class<? extends Object> objectClass = pointeurObjet.getClass();
		Class<?>[] classTypes = null;
		Method method = null;
		Object res = null;
		try {
			method = objectClass.getMethod(nomFonction, classTypes);
			res = method.invoke(pointeurObjet, valeurs);
		} catch (Exception e){
			m_sendBackMessage = "Error : " + e.getMessage();
		}
		if(res != null){
			m_sendBackMessage = "Success : " + res.toString();
		}
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
			ApplicationServeur appServeur = new ApplicationServeur(port,repertoireSource, repertoireClasses);
			appServeur.aVosOrdre();
		} catch (IOException e) {
			System.err.println("Server Error: " + e.getMessage());
	        System.err.println("Localized: " + e.getLocalizedMessage());
	        System.err.println("Stack Trace: " + e.getStackTrace());
	        System.err.println("To String: " + e.toString());
		}

	}

}
