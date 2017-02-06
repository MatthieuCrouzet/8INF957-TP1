package ca.uqac.inf853.tp1;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Baptiste Buron, Matthieu Crouzet
 * ApplicationServeur initialise une connexion reçoit les objets de type Commande envoyé par le client
 * execute les commandes et renvoie les résultats au programme client
 */
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
	 * @param repertoireClasses : le chemin du repertoire contenant les classes à compiler
	 * @param repertoireSource : le chemin du repertoire contenant les fichiers sources pour les charger
	 * @throws IOException 
	 */
	public ApplicationServeur(int port, String repertoireSource, String repertoireClasses) throws IOException{
		m_port = port;
		m_server_socket = new ServerSocket(m_port);
		m_repertoireSource = repertoireSource;
		m_repertoireClasses = repertoireClasses;
		m_classLoader = new URLClassLoader(new URL[] { new File(m_repertoireClasses).toURI().toURL() });
		m_classesLoaded = new ArrayList<Class<?>>();
		m_classesLoaded.add(Float.TYPE);
		m_classesLoaded.add(String.class);
		m_classesLoaded.add(int.class);
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
	 * @param uneCommande : la commande à traiter
	 */
	public void traiteCommande(Commande uneCommande){
		String[] detailCommande = uneCommande.getCommandeDescription().split("#");
		switch(detailCommande[0]){
		// On gère six cas de commande à exécuter : compilation / chargement / creation / lecture / ecriture / fonction
			case "compilation":
				//on lance la méthode traiterCompilation()
				traiterCompilation(detailCommande[1]);
				break;
			case "chargement":
				//on lance la méthode traiterChargement()
				traiterChargement(detailCommande[1]);
				break;
			case "creation":
				//on lance la méthode traiterCréation()
				
				try {
					Class<?> classeDeLobjet = Class.forName(detailCommande[1]);
					traiterCreation(classeDeLobjet, detailCommande[2]);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "lecture":
				// On lance la méthode traiterLecture()
				
				Object pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				traiterLecture(pointeurObjet, detailCommande[2]);
				break;
			case "ecriture":
				//On lance la méthode traiterEcriture()
				
				pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				traiterEcriture(pointeurObjet, detailCommande[2], detailCommande[3]);
				break;
				
			case "fonction":
				//On lance la méthode traiteAppel()
			
				pointeurObjet = m_objectsCreated.get(detailCommande[1]);
				String[] types = null;
				Object[] valeurs = null;
				if(detailCommande.length>3){
					String[] args = detailCommande[3].split(",");
					types = new String[args.length];
					valeurs = new Object[args.length];
					for(int index=0; index < args.length; index++){
						String[] keyValue = args[index].split(":");
						types[index] = keyValue[0];
						for (Class<?> current : m_classesLoaded) {
							if (current.getName().equals(types[index])) {
									//Si le type demande correspond à une des classes du registraire on recupere une instance 
									if(keyValue[1].contains("ID")){
										valeurs[index] = m_objectsCreated.get(keyValue[1].substring(keyValue[1].indexOf("(")+1, keyValue[1].indexOf(")")));
									}
									//Sinon, on cree l'objet en le parsant selon le type demande
									else{
										valeurs[index] = parse(current, keyValue[1]);
									}
							}
						}
					}
				}
				
				traiterAppel(pointeurObjet, detailCommande[2], types, valeurs);
				
				break;
		}
		
	}
	
	/**
	 * methode privée pour convertir une chaine de caractère selon type primitif voulue
	 * @param current : classe pouvant représenter un type primitif
	 * @param value : chaine de caractère à convertir
	 * @return la valeur convertie
	 */
	private Object parse(Class<?> current, String value) {
		if(current == Float.TYPE){
				return Float.parseFloat(value);
		}
		else if(current == int.class){
			return Integer.parseInt(value);
		}
			
		return value;
	}

	/**
	 * traiterLecture : traite la lecture d'un attribut. Renvoie le résultat par le
	 * socket
	 * @param pointeurObjet : l'objet sur lequel on lit l'attribut
	 * @param attribut : l'attribut à lire
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
	 * @param pointeurObjet : l'objet sur lequel on redéfinit un attribut
	 * @param attribut : l'attribut qui doit etre modifie
	 * @param valeur : l'objet qui va modifier l'attribut
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
	 * @param classeDeLobjet : la classe dont on souhaite créer une instance
	 * @param identificateur : l'indentifiant le l'instance nouvellement créée
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
	 * @param nomQualifie : le nom de la classe à charger
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
	 * @param cheminRelatifFichierSource : le nom des fichiers à compiler
	 */
	public void traiterCompilation(String cheminRelatifFichierSource){
		String[] sources = cheminRelatifFichierSource.split(",");    	
    	try {
    		for(int i = 0; i < sources.length; i++){
    			//On éxécute la commande de compilation autant de fois qu'il y a de fichier à compiler
	    		String command = "javac " + m_repertoireSource + sources[i].substring(5); 
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
	 * @param pointeurObjet : L'instance sur lequel on execute la fonction
	 * @param nomFonction : le nom de la fonction à exécuter
	 * @param types: les types des paramètres mis dans la fonction appelée
	 * @param valeurs : les paramètres mis dans la fonction appelée
	 */
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types, Object[] valeurs){
		Class<? extends Object> objectClass = pointeurObjet.getClass();
		Class<?>[] classTypes = null;
		if (types != null){
			classTypes = new Class<?>[types.length];
			for (int i = 0; i < types.length; i++) {
				for (Class<?> current : m_classesLoaded) {
					if (current.getName().equals(types[i])) {
						classTypes[i] = current;
						break;
					}
				}
				if (classTypes[i] == null) {
					m_sendBackMessage = "Error : cannot find a type";
					break;
				}				
			}
		}
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
	 * programme principal. Prend 4 arguments: 1) numéro de port, 2) répertoire source,
	 * 3) répertoire classes et 4) nom du fichier de traces (sortie)
	 * Cette méthode crée une instance de la classe ApplicationServeur, l'initialise
	 * puis appele aVosOrdres() sur cet objet
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
