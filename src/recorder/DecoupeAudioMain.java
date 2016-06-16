package recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * @author Jean-Baptiste Gu�ry
 *
 */
public class DecoupeAudioMain {
	
	// Le tableau des valeurs � moyenner
	private long[] tableau;
	// L'index du dernier �l�ment du tableau
	private int index_tableau;
	// Indique si on a d�j� boucl�
	private boolean tableau_boucle;
	// La somme de tous les �l�ments du tableau
	private long somme;
	
	public static void main(String[] args) throws IOException{
		if(args.length < 1){
			System.out.println("Passer le pr�fixe d'une fichier en param�tre 1");
			System.exit(0);
		}
		//test(); // Utilis� pour effectuer des tests
		String fichier_prefixe = args[0];
		
		// Valeurs empiriques
		double tps_silence = 0.2; // Temps en secondes pour consid�rer un silence
		double tolerance_silence_fin = 8000.; // Temps de tol�rence pour la d�tection du silence de fin d'une musique en millisecondes
		double seuil_silence = 120.; // Seuil en dessous duquel on consid�re qu'il y a du silence
		double seuil_bruit = 120.; // Seuil au dessus duquel on consid�re qu'il y a du bruit

		try {
			// La liste des fichiers audio
			List<File> fichiers = new ArrayList<File>();
			fichiers.add(new File(fichier_prefixe+".wav"));
			// On r�cup�re la liste de lecture avec les infos sur les musiques
			List<InstructionDecoupe> liste_instructions = new ArrayList<InstructionDecoupe>();
			BufferedReader br = new BufferedReader(new FileReader(new File(fichier_prefixe+".txt")));
			String line = "";
			while((line = br.readLine()) != null){
				String[] tabs = line.split("	");
				if(tabs.length == 4){
					String[] dec = tabs[3].split(":");
					liste_instructions.add(new InstructionDecoupe((Integer.parseInt(dec[0])*60 + Integer.parseInt(dec[1]))*1000, tabs[0], tabs[1], tabs[2]));
				}
			}
			br.close();
			
			// Si la liste de lecture est vide pas la peine de continuer on ne pourra pas d�couper
			if(liste_instructions.isEmpty()){
				System.out.println("Liste de lecture vide");
				System.exit(0);
			}else{
				System.out.println(liste_instructions.size()+" pistes � d�couper r�parties dans "+fichiers.size()+" fichiers");
			}
			
			int numero_piste = 0; // Le num�ro de la piste suppos�e �tre analys�e
			InstructionDecoupe i_courant = liste_instructions.get(numero_piste);

			// On boucle sur les fichiers donn�s en param�tre
			AudioInputStream ais;

			boucle_pincipale: // Label de la boucle prinicpale lorsque la liste des musiques � d�couper est vide
			for(File f : fichiers){
				ais = AudioSystem.getAudioInputStream(f);
				AudioFormat af = ais.getFormat();
				System.out.println("Ouverture de "+f);
				System.out.println("Encodage : "+af.getEncoding()+", "+af.getSampleRate()+" Hz, "+af.getSampleSizeInBits()+" bits "+af.getChannels()+" ch => "+af.getFrameSize()+" octets par page");
				int taille_groupe_echantillon = af.getFrameSize();
				
				int taille_fenetre = (int)(af.getSampleRate() * tps_silence);
				// Instanciation des moyenneurs fen�tr�s
				DecoupeAudioMain dam1 = new DecoupeAudioMain(taille_fenetre);
				DecoupeAudioMain dam2 = new DecoupeAudioMain(taille_fenetre);

				int taille_buffer = af.getFrameSize() * 65536; // Le buffer temporaire doit �tre un multiple du nombre de bytes par page du fichier audio
				long frequence_ech = (int)af.getSampleRate(); // La fr�quence d'�chantillonage 
				long lus = 0; // Le nombre de bytes copi�s dans le buffer � chaque remplissage de celui-ci
				byte[] echantillons = new byte[taille_buffer]; // Le buffer d'�chantillons
				boolean attente_silence = false; // Pour savoir si on est en attente de silence (attente d'un front descendant) ou non (attente d'un front montant)
				long derniere_page_debut_bruit = 0; // Le num�ro de page du dernier d�but de bruit

				// Tant qu'on lit le fichier audio
				int index = 0;
				long numero_page = 0;
				while((lus = ais.read(echantillons, 0, taille_buffer)) > 0){
					// Lecture des �chantillons du buffer
					for(index = 0; index < lus; index+=taille_groupe_echantillon){
						double val1 = dam1.calculeMoyenne(Math.abs((echantillons[index+1] << 8) + echantillons[index]));
						double val2 = dam2.calculeMoyenne(Math.abs((echantillons[index+3] << 8) + echantillons[index+2]));
						// D�tection d'une d�but de silence ou de bruit
						boolean bdebut_bruit = !attente_silence && val1 >= seuil_bruit && val2 >= seuil_bruit;
						boolean bdebut_silence = attente_silence && val1 < seuil_silence && val2 < seuil_silence; 
						// Si c'est le d�but d'un silence ou d'un bruit
						if(bdebut_bruit || bdebut_silence){
							// D�bug
							long numero_page_offset;
							if(bdebut_bruit){
								numero_page_offset = numero_page - taille_fenetre / 2; // On enl�ve 1/2e de la fen�tre moyenneuse pour corriger le d�calage de d�tection du bruit (empirique)
								if(numero_page_offset < 0)
									numero_page_offset = 0;
								derniere_page_debut_bruit = numero_page_offset;
								attente_silence = true;
								i_courant.debut = (long)((double)(numero_page_offset*1000)/(double)frequence_ech);
								i_courant.source = f.getAbsolutePath();
								System.out.print("Musique n�"+(numero_piste+1)+"  de  "+milliSecondesVersHMS(i_courant.debut));
							}else{
								// numero de page corrig�
								numero_page_offset = numero_page + taille_fenetre / 2; // On ajoute 1/2e de la fen�tre moyenneuse pour corriger le d�calage de d�tection du bruit (empirique)
								// On regarde si le silence concorde avec la fin suppos�e de la musique +/- un offset
								if(Math.abs(((Math.abs(derniere_page_debut_bruit - numero_page)*1000) / (double)frequence_ech) - i_courant.duree_theorique) < tolerance_silence_fin){
									i_courant.fin = (long)((double)(numero_page_offset*1000)/(double)frequence_ech);
									long duree_reelle = i_courant.fin-i_courant.debut;
									System.out.println("  �  "+milliSecondesVersHMS(i_courant.fin)+",  dur�e th�orique "+milliSecondesVersHMS(i_courant.duree_theorique)+
											",  r�elle "+milliSecondesVersHMS(duree_reelle)+",  �crart "+milliSecondesVersHMS(Math.abs(duree_reelle-i_courant.duree_theorique)));
									attente_silence = false;
									numero_piste++;
									// S'il y a encore des musiques � d�couper
									if(numero_piste < liste_instructions.size()){
										i_courant = liste_instructions.get(numero_piste);
									}else{
										break boucle_pincipale;
									}
								}
							}
						}
						// Incr�mentation du num�ro de page
						numero_page++;
					}
				}
				if(numero_page == 0)
					System.err.println("Rien lu. Fichier trop gros ?");
				// Si on attend toujours le silence et qu'on est � la fin du fichier
				else if(attente_silence)
					if(Math.abs((Math.abs(derniere_page_debut_bruit - numero_page)*1000 / (double)frequence_ech) - i_courant.duree_theorique) < tolerance_silence_fin){
						i_courant.fin = (long)((double)(numero_page*1000)/(double)frequence_ech);
						long duree_reelle = i_courant.fin-i_courant.debut;
						System.out.println("  �  "+milliSecondesVersHMS(i_courant.fin)+",  dur�e th�orique "+milliSecondesVersHMS(i_courant.duree_theorique)+
								",  r�elle "+milliSecondesVersHMS(duree_reelle)+",  �crart "+milliSecondesVersHMS(Math.abs(duree_reelle-i_courant.duree_theorique)));
						attente_silence = false;
						numero_piste++;
						if(numero_piste >= liste_instructions.size())
							break;
						i_courant = liste_instructions.get(numero_piste);
					}else{
						System.out.println();
						System.err.println("Fin du fichier atteint sans respecter la constrainte du temps de la musique n�"+(numero_piste+1)+" de "+milliSecondesVersHMS(i_courant.duree_theorique*1000));
						System.exit(1);
					}
				attente_silence = false;
				ais.close();
			}
			System.out.println("Ecriture des instructions...");
			dumpInstructionsDecoupe(liste_instructions, fichier_prefixe+".bat");
			System.out.println("FIN");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	public DecoupeAudioMain(int taille_fenetre){
		this.resetMoyenneur(taille_fenetre);
	}
	
	/**
	 * Remet � z�ro le calcule du moyenneur
	 */
	public void resetMoyenneur(int tailleFenetre){
		tableau = new long[tailleFenetre];
		index_tableau = -1;
		somme = 0;
	}
	
	/**
	 * Rajoute une mesure dans le calcul de la moyenne avant de la retourner
	 * @param val
	 * @return
	 */
	public double calculeMoyenne(long val){
		// Incr�mentation de l'index du tableau (et bouclage si n�cessaire)
		index_tableau = (index_tableau + 1) % tableau.length;
		// Ajout de la valeur � la somme
		somme += val;

		// Si on a d�j� boucl�, on retire la derni�re valeur du tableau � la somme
		if(tableau_boucle){
			somme -= tableau[index_tableau];
			tableau[index_tableau] = val;
			return (double)somme / (double)tableau.length;
		}else{
			tableau[index_tableau] = val;
			// Si on arrive � la taille ma du tableau, il sera temps de boucler (tableau circulaire)
			if(tableau.length-1 == index_tableau)
				tableau_boucle = true;
			return (double)somme / (double)(index_tableau+1);
		}
	}
	
	/**
	 * Renvoie al taille de la fen�tre du moyenneur
	 * @return
	 */
	public int getTailleFenetre(){
		return tableau.length;
	}
	
	/**
	 * Retourne la somme courante des valeurs adns la fen�tre du moyenneur
	 * @return
	 */
	public long getSomme(){
		return somme;
	}
	
	/**
	 * Retourne la taille courante de la fen�tre du moyenneur
	 * @return valeur < getTailleFenetre() si le moyenneur est en initialisation, valeur �gale getTailleFenetre() sinon
	 */
	public long getTailleCourante(){
		return tableau_boucle ? tableau.length : index_tableau+1;
	}
	
	/**
	 * Convertit une valeur donn�es en secondes en Heures:Minutes:Secondes sous la forme HH:MM:ss.ms
	 * @return
	 */
	public static String milliSecondesVersHMS(long valeur){
		long reste_mod_heures = valeur % 3600000;
		long heures = (valeur - reste_mod_heures) / 3600000;
		long reste_mod_minutes = reste_mod_heures % 60000;
		long minutes = (reste_mod_heures - reste_mod_minutes) / 60000;
		long millisecondes = reste_mod_minutes % 1000;
		long secondes = (reste_mod_minutes - millisecondes) / 1000;
		return heures+":"+(minutes<10 ? "0" : "")+minutes+":"+(secondes<10 ? "0" : "")+secondes+"."+(millisecondes < 10 ? "00" : (millisecondes < 100 ? "0" : ""))+millisecondes;
	}
	public static String milliSecondesVersSSMMM(long valeur){
		long millis = valeur % 1000;
		long secondes = (valeur-millis)/1000;
		return secondes+"."+millis;
	}
	
	/**
	 * Ecrit une liste d'instructions de d�coupe dans un fichier de dump
	 * @param liste
	 */
	private static void dumpInstructionsDecoupe(List<InstructionDecoupe> liste, String nom_fichier){
		try {
			PrintStream ps = new PrintStream(new File(nom_fichier));
			for(InstructionDecoupe id : liste){
				// Colonnes : fichier wave source, dur�e th�orique de la musiquen dur�e r�elle, temps de d�but, temps de fin, nom de la usique, nom de l'artiste, nom de l'album
				// ffmpeg -ss <temps_debut> -t <duree ss.mmm> -i <fichirer_source> -acodec copy <fichier dest>
				ps.println("ffmpeg.exe -ss "+
						milliSecondesVersSSMMM(id.debut)+
						" -t "+
						milliSecondesVersSSMMM(id.fin-id.debut)+
						" -i \""+
						id.source.replace(".wav", ".mp3")+
						"\" -acodec copy "+
						"\""+
						id.nom_artiste+
						" - "+
						id.nom_musique+
						".mp3\"");
			}
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static class InstructionDecoupe{
		
		long debut; // Temps en millisecondes
		long fin; // Temps en millisecondes
		String source; // Chemin compl�te du fichier
		
		long duree_theorique; // En millisecondes
		String nom_musique; // Cha�nes de caract�res
		String nom_artiste; // Cha�nes de caract�res
		String nom_album; // Cha�nes de caract�res
		
		public InstructionDecoupe(long duree_theorique, String nom_musique, String nom_artiste, String nom_album){
			this.duree_theorique = duree_theorique;
			this.nom_musique = nom_musique;
			this.nom_artiste = nom_artiste;
			this.nom_album = nom_album;
			debut = 0;
			fin = 0;
			source = "null";
		}
	}
	
}
