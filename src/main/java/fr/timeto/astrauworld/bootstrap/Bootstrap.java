package fr.timeto.astrauworld.bootstrap;

import fr.theshark34.openlauncherlib.LaunchException;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static fr.timeto.timutilslib.PopUpMessages.*;
import static fr.timeto.timutilslib.CustomFonts.*;
import static fr.timeto.timutilslib.TimFilesUtils.*;

public class Bootstrap {

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

    @SuppressWarnings("all")
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
        String[] javaHomeSplit1 = javaHome.split(";");
        String pattern = Pattern.quote(separatorChar);
        String[] javaHomeSplit2 = javaHomeSplit1[0].split(pattern);
        String firstReferencedJavaVersion = javaHomeSplit2[javaHomeSplit2.length - 1];
        String[] javaHomeSplit3 = firstReferencedJavaVersion.split("\\.");
        String firstReferencedJavaGlobalVersion = javaHomeSplit3[0];
        System.out.println("%JAVA_HOME%: " + javaHome);
        System.out.println("First referenced java: " + javaHomeSplit1[0]);
        System.out.println("Last part of first referenced java: " + firstReferencedJavaVersion);
        System.out.println("First referenced java global version: " + firstReferencedJavaGlobalVersion);

        if (firstReferencedJavaGlobalVersion.contains("17")) {
            System.out.println("Java 17 détecté");
            infosLabel.setText("Java 17 d\u00e9tect\u00e9");
        } else {
            Thread t = new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/AstrauworldMC/launcher/wiki/Mise-en-place-de-Java-17"));
                    System.exit(0);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            normalMessage("Java 17 non d\u00e9tect\u00e9", "Vous avez besoin de  Java 17, cliquez OK", t);
        }

    }

    static void launch() throws LaunchException, IOException {
        String [] parts = System.getenv("JAVA_HOME").split( ";" );
        String cmd = "\"" + parts[0] + separatorChar + "bin" + separatorChar + "java" + "\" -cp \"" + launcherJar + "\" fr.timeto.astrauworld.launcher.LauncherFrame";

        System.out.println("Commande: " + cmd);

        Runtime rt = Runtime.getRuntime();

        Process pr = rt.exec(cmd);

        System.exit(0);
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

        try {
            infosLabel.setText("Lancement...");
            launch();
        } catch (LaunchException | IOException e) {
            errorMessage("Erreur", "Erreur au lancement  du launcher");
            throw new RuntimeException(e);
        }

    }
}
