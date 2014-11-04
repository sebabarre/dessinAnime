import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.InputFormatException;
import it.sauronsoftware.jave.MultimediaInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class Main {

	static ArrayList<String> list = new ArrayList<String>();
	static String empVlc = "";

	public static void main(String[] args) {
		String path = getPathAnimate();
		empVlc = getEmplacementVLC();
		long dureeTotale = getDuree();
		dureeTotale = dureeTotale * 60 * 1000;
		long duree = 0;
		File firstOne = getOneToLaunch(path, dureeTotale, 0);
		addPlaylist(firstOne);
		ArrayList<String> listFichiers = getAllPath(path, dureeTotale);
		listFichiers.remove(firstOne.getAbsolutePath());
		if (listFichiers.size() == 0) {
			JOptionPane.showMessageDialog(null, "Soit vous n'avez pas de dessin animé, soit la durée indiquée est trop courte.");
			System.exit(0);
		}
		duree += getDuree(firstOne);
		File f = null;
		while ((dureeTotale >= duree) && (listFichiers.size() != 0)) {
			f = pickAVideo(listFichiers);
			long dureeDeF = getDuree(f);
			if ((duree + dureeDeF) <= (dureeTotale + (5*60*1000))) {
				list.add(f.getName());
				addPlaylist(f);
				listFichiers.remove(f.getAbsolutePath());
				duree += dureeDeF;
			}
		}
		System.exit(0);
	}
	
	private static File getOneToLaunch(String path, long dureeTotale, int nbreEssai) {
		String originalPath = getPathAnimate();
		if (path.length() < originalPath.length())
			path = originalPath;
		File f = new File(path);
		File[] listFile = f.listFiles();
		int indice = (int) (Math.random() * (listFile.length));
		File newFile = listFile[indice];
		nbreEssai ++;
		if (newFile.isDirectory())
			return getOneToLaunch(newFile.getAbsolutePath(), dureeTotale, nbreEssai);
		else if (newFile.isFile()) {
			if (isVideo(newFile) && (dureeTotale > getDuree(newFile))) {
				return newFile;
			} else {
				if (nbreEssai == 100) {
					JOptionPane.showMessageDialog(null, "La recherche d'un fichier est trop longue. Ya un problème, " +
							"soit dans le nombre de minutes que vous avez rentré, soit dans le paramétrage de vos vidéos.");
					System.exit(0);
					return null;
				} else
					return getOneToLaunch(new File(newFile.getParentFile().getAbsolutePath()).getParentFile().getAbsolutePath(), dureeTotale, nbreEssai);

			}
		}
		return getOneToLaunch(path, dureeTotale, nbreEssai);
	}

	private static String getEmplacementVLC() {
		String path = "C:\\Users\\Default\\Documents\\empVLC.text";
		File file = new File(path);
		if (!file.exists()) {
			configureFileVlc(file);
			path = getPathAnimate();
		} else {}
			path = getPath(file);
		return path;
	}

	private static void configureFileVlc(File file) {
		String path = "";
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(null);
		fc.setDialogTitle("Veuillez sélectionner vlc.exe");
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showOpenDialog(new JOptionPane()) == JFileChooser.APPROVE_OPTION) {
			path = fc.getSelectedFile().getAbsolutePath();
			if (!isItVlc(fc.getSelectedFile()))
				configureFileVlc(file);
			else {
				FileWriter fw = null;
				try {
					fw = new FileWriter(file, false);
					fw.write(path);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					System.exit(0);
				} finally {
					if (fw != null) {
						try {
							fw.close();
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
							System.exit(0);
						}
					}
				}
			}
		}
		else
			System.exit(0);
	}

	private static boolean isItVlc(File selectedFile) {
		if (!selectedFile.getName().equals("vlc.exe")) {
			JOptionPane.showMessageDialog(null, "Le programme choisi n'est pas VLC");
			return false;
		}
		return true;
	}

	private static ArrayList<String> getAllPath(String path, long dureeTotale) {
		File f = new File(path);
		ArrayList<String> list = new ArrayList<String>();
		File[] listFile = f.listFiles();
		for (int i=0; i<listFile.length ; i++) {
			File file = listFile[i];
			if (file.isDirectory())
				list.addAll(getAllPath(file.getAbsolutePath(), dureeTotale));
			else {
				if (isVideo(file)) {
					if (getDuree(file) < dureeTotale)
						list.add(file.getAbsolutePath());
				}
			}
		}
		return list;
	}

	private static boolean isVideo(File file) {
		String type = "";
		try {
			type = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
		} catch (Exception e) {
			return false;
		}
        if (type.equalsIgnoreCase(".avi") || type.equalsIgnoreCase(".mkv") || type.equalsIgnoreCase(".mpg") || type.equalsIgnoreCase(".divx"))
        	return true;
        return false;
	}

	private static long getDuree(File f) {
		Encoder encoder = new Encoder();
		MultimediaInfo infos = null;
		try {
			infos = encoder.getInfo(f);
		} catch (InputFormatException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			System.exit(0);
		} catch (EncoderException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			System.exit(0);
		}
		Long duration = infos.getDuration();
		return duration;
	}

	private static File pickAVideo(ArrayList<String> listFichiers) {
		int randomIndice = (int) (Math.random() * (listFichiers.size() -1));
		File f = new File(listFichiers.get(randomIndice));
		return f;
	}

	public static void addPlaylist(File f) {
		try {
			Runtime.getRuntime().exec(new String[]{empVlc,f.getAbsolutePath()});
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			System.exit(0);
		}
	}
	
	public static String getPathAnimate() {
		String path = "C:\\Users\\Default\\Documents\\javaAnimatePath.text";
		File file = new File(path);
		if (!file.exists()) {
			configureFile(file);
			path = getPathAnimate();
		} else {}
			path = getPath(file);
		return path;
	}
	
	private static String getPath(File file) {
		String path = "";
		FileReader fr = null;
		try {
			fr = new FileReader(file);
			Scanner scanner = new Scanner(fr);
			while (scanner.hasNextLine())
				path = scanner.nextLine();
			scanner.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			System.exit(0);
		} finally {
			try {
				fr.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return path;
	}

	private static void configureFile(File file) {
		String path = "";
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(null);
		fc.setDialogTitle("Veuillez sélectionner le dossier où se trouve tout vos dessins animés");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			path = fc.getSelectedFile().getAbsolutePath();
			FileWriter fw = null;
			try {
				fw = new FileWriter(file, false);
				fw.write(path);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				System.exit(0);
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						System.exit(0);
					}
				}
			}
		}
		else
			System.exit(0);
	}

	public static int getDuree() {
		boolean control = false;
		int dureeTotale = 0;
		while (!control) {
			try {
				String dureeTotal = JOptionPane.showInputDialog(new JPanel(), "Indiquez une durée maximum en minutes",
						"Durée totale d'abrutissement", JOptionPane.QUESTION_MESSAGE);
				dureeTotale = Integer.valueOf(dureeTotal);
				if (dureeTotale <= 0)
					control = false;
				control = true;
			} catch (Exception e) {
				control = false;
			}
		}
		return dureeTotale;
	}
}
