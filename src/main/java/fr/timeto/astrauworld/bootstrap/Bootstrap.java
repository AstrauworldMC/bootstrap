package fr.timeto.astrauworld.bootstrap;

import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.openlauncherlib.util.SplashScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.Thread.sleep;

public class Bootstrap {

    private static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = Bootstrap.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    public static Font CustomFont(String path) {
        Font customFont = loadFont(path, 24f);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);
        return customFont;

    }
    public static Font loadFont(String path, float size){
        try {
            Font myFont = Font.createFont(Font.TRUETYPE_FONT, getFileFromResourceAsStream(path));
            return myFont.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static Font kollektifFont;
    public static Font kollektifBoldFont;
    public static Font kollektifBoldItalicFont;
    public static Font kollektifItalicFont;
    public static Font minecraftiaFont;
    private static final String FONT_PATH_KOLLEKTIF = "fonts/Kollektif.ttf";
    private static final String FONT_PATH_KOLLEKTIFBOLD = "fonts/Kollektif-Bold.ttf";
    private static final String FONT_PATH_KOLLEKTIFBOLDITALIC = "fonts/Kollektif-BoldItalic.ttf";
    private static final String FONT_PATH_KOLLEKTIFITALIC = "fonts/Kollektif-Italic.ttf";
    private static final String FONT_PATH_MINECRAFTIA = "fonts/Minecraftia-Regular.ttf";

    static SplashScreen splash = new SplashScreen("Astrauworld Launcher", Swinger.getResourceIgnorePath("/splash.png"));
    static JPanel panel = splash.getContentPane();
    static BufferedImage loadingImage = Swinger.getResourceIgnorePath("/loading.gif");
    static ImageIcon loadingIcon = new ImageIcon(Swinger.getResourceIgnorePath("/loading.gif"));
    static JLabel loadingLabel = new JLabel();

    static String separatorChar = System.getProperty("file.separator");
    static String userAppDataDir = System.getenv("APPDATA");
    static String astrauworldDir = userAppDataDir + separatorChar + "Astrauworld Launcher";

    static String currentPropertiesDir = astrauworldDir + separatorChar + "currentLauncher.properties";
    static String newPropertiesDir = astrauworldDir + separatorChar + "newLauncher.properties";
    static String launcherJar = astrauworldDir + separatorChar + "launcher.jar";

    static File astrauworldFolder = new File(astrauworldDir);
    static File currentPropertiesFile = new File(currentPropertiesDir);
    static File newPropertiesFile = new File(newPropertiesDir);
    static File launcherJarFile = new File(launcherJar);

    static Path currentPropertiesPath = Paths.get(currentPropertiesDir);
    static Path newPropertiesPath = Paths.get(newPropertiesDir);

    static Saver currentSaver = new Saver(currentPropertiesPath);
    static Saver newSaver = new Saver(newPropertiesPath);

    static JLabel infosLabel = new JLabel("V\u00e9rification de test", SwingConstants.CENTER);

    public static void initFonts() {
        kollektifFont = CustomFont(FONT_PATH_KOLLEKTIF);
        kollektifBoldFont = CustomFont(FONT_PATH_KOLLEKTIFBOLD);
        kollektifBoldItalicFont = CustomFont(FONT_PATH_KOLLEKTIFBOLDITALIC);
        kollektifItalicFont = CustomFont(FONT_PATH_KOLLEKTIFITALIC);
        minecraftiaFont = CustomFont(FONT_PATH_MINECRAFTIA);

    }

    static BufferedImage rotatingImage(BufferedImage image, int rad) {


        final double rads = Math.toRadians(rad);
        final double sin = Math.abs(Math.sin(rads));
        final double cos = Math.abs(Math.cos(rads));
        final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
        final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
        final AffineTransform at = new AffineTransform();
        at.translate(w / 2, h / 2);
        at.rotate(rads,0, 0);
        at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(image,rotatedImage);

        return rotatedImage;

    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void downloadFromInternet(String fileUrl, File dest) throws IOException {
        URL url = new URL(fileUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(dest);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    private static void copyFile(File src, File dest) throws IOException {
        // Créer l'objet File Reader
        FileReader fr = new FileReader(src);
        // Créer l'objet BufferedReader
        BufferedReader br = new BufferedReader(fr);
        // Créer l'objet File Writer
        FileWriter fw = new FileWriter(dest);
        String str;
        // Copie le contenu dans le nouveau fichier
        while((str = br.readLine()) != null)
        {
            fw.write(str);
            fw.write(System.lineSeparator());
            fw.flush();
        }
        fw.close();
    }

    static String getJarLink() {
        return "https://github.com/AstrauworldMC/launcher/releases/download/" + newSaver.get("launcherVersion") + "/launcher.jar";
    }

    static void setPropertiesFile() {
        infosLabel.setText("V\u00e9rification des infos de la derni\u00e8re version");

        try {
        newPropertiesFile.createNewFile();
        } catch (IOException ignored) {}


        try {
            downloadFromInternet("https://raw.githubusercontent.com/AstrauworldMC/launcher/main/currentLauncher.properties", newPropertiesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (currentSaver.get("launcherVersion") == null) {
            try {
                currentPropertiesFile.createNewFile();
                System.out.println("created");
            } catch (IOException ignored) {}
            try {
                copyFile(newPropertiesFile, currentPropertiesFile);
                System.out.println("copied?");
            } catch (IOException e) {
                System.out.println("fail");
                throw new RuntimeException(e);
            }
        }
    }

    static void updateJar() {
        infosLabel.setText("Mise à jour du launcher");

        try {
            launcherJarFile.createNewFile();
            System.out.println("jar created");
        } catch (IOException ignored) {}

        if (!Objects.equals(currentSaver.get("launcherVersion"), newSaver.get("launcherVersion"))) {
            System.out.println(currentSaver.get("launcherVersion"));
            System.out.println(newSaver.get("launcherVersion"));
            System.out.println("pas egal");
            infosLabel.setText("T\u00e9l\u00e9chargement de la derni\u00e8re version...");
            try {
                downloadFromInternet(getJarLink(), launcherJarFile);
                System.out.println("jar downloaded?");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                copyFile(newPropertiesFile, currentPropertiesFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            infosLabel.setText("Derni\u00e8re version d\u00e9tect\u00e9e");
        }
    }

    static void update() {
        astrauworldFolder.mkdir();

        setPropertiesFile();

        updateJar();

        infosLabel.setText("V\u00e9rification de Java 17");
        String javaHome = System.getenv("JAVA_HOME");
        System.out.println(javaHome);

        if (javaHome.contains("17")) {
            System.out.println("Java 17 détecté");
            infosLabel.setText("Java 17 d\u00e9tect\u00e9");
        } else {
            Thread t = new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/AstrauworldMC/launcher/wiki/Mise-en-place-de-Java-17"));
                    System.exit(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            PopUpMessages.normalMessage("Java 17 non d\u00e9tect\u00e9", "Vous avez besoin de  Java 17, cliquez OK", t);
        }

    }

    public static void main(String[] args) {
        initFonts();

        splash.setIconImage(Swinger.getResourceIgnorePath("/icon.png"));
        splash.setSize(346, 446);
        panel.setLayout(null);

        loadingLabel.setBounds(14, 59, 319, 328);
        splash.add(loadingLabel);

        AtomicInteger i = new AtomicInteger(1);
        Thread t = new Thread(() -> {
            while(i.get() < 361) {
                loadingLabel.setIcon(new ImageIcon(rotatingImage(loadingImage, i.get())));
                i.addAndGet(1);

                if (i.get() == 360) {
                    i.set(1);
                }

                try {
                    sleep(4);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }); // TODO réussir à ajouter un loading qui marche mdrr
    //    t.start();

        infosLabel.setBounds(34, 375, 278, 20);
        infosLabel.setForeground(Color.WHITE);
        infosLabel.setFont(kollektifFont.deriveFont(16f));
        splash.add(infosLabel);

        splash.display();

        update();

    }
}
