package fr.timeto.astrauworld.bootstrap;

import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.swinger.colored.SColoredBar;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

import static fr.theshark34.swinger.Swinger.getTransparentWhite;
import static fr.timeto.timutilslib.PopUpMessages.*;
import static fr.timeto.timutilslib.CustomFonts.*;
import static fr.timeto.timutilslib.TimFilesUtils.*;

public class Bootstrap {

    static SplashScreen splash = new SplashScreen("Astrauworld Launcher", Swinger.getResourceIgnorePath("/splash.png"));
    static JPanel panel = splash.getContentPane();

    static String separatorChar = System.getProperty("file.separator");
    static String userAppDataDir = System.getenv("APPDATA");
    static String astrauworldDir = userAppDataDir + separatorChar + "Astrauworld Launcher";

    static String currentPropertiesDir = astrauworldDir + separatorChar + "launcher.properties";
    static String oldCurrentPropertiesDir = astrauworldDir + File.separator + "currentLauncher.properties";
    static String newPropertiesDir = astrauworldDir + separatorChar + "newLauncher.properties";
    static String launcherJar = astrauworldDir + separatorChar + "launcher.jar";

    static File astrauworldFolder = new File(astrauworldDir);
    static File currentPropertiesFile = new File(currentPropertiesDir);
    static File oldCurrentPropertiesFile = new File(oldCurrentPropertiesDir);
    static File newPropertiesFile = new File(newPropertiesDir);
    static File launcherJarFile = new File(launcherJar);

    static Path currentPropertiesPath = Paths.get(currentPropertiesDir);
    static Path newPropertiesPath = Paths.get(newPropertiesDir);

    static Saver currentSaver = new Saver(currentPropertiesPath);
    static Saver newSaver = new Saver(newPropertiesPath);

    static JLabel infosLabel = new JLabel("Si vous voyez ca, c'est pas bien", SwingConstants.CENTER);
    static SColoredBar progressBar = new SColoredBar(getTransparentWhite(25), Color.RED);
    static JLabel percentLabel = new JLabel("", SwingConstants.RIGHT);
    static JLabel bytesLabel = new JLabel("", SwingConstants.LEFT);

    static void println(String str) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "] [Astrauworld Bootstrap] " + str);
    }

    static String getJarLink() {
        return "https://github.com/AstrauworldMC/launcher/releases/download/" + newSaver.get("launcherVersion") + "/launcher.jar";
    }

    static void setPropertiesFile() {
        infosLabel.setText("V\u00e9rification de la derni\u00e8re version");

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
                println("created");
            } catch (IOException ignored) {}
        }
    }

    static void updateJar() {
        setPropertiesFile();

        println("");
        println("---- JAR UPDATE ----");

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        infosLabel.setText("Mise \u00e0 jour du launcher");

        try {
            if (launcherJarFile.createNewFile()) {
                currentSaver.set("launcherVersion", "");
                println("jar created");
            }
        } catch (IOException ignored) {}

        if (!Objects.equals(currentSaver.get("launcherVersion"), newSaver.get("launcherVersion"))) {
            println("Current: " + currentSaver.get("launcherVersion"));
            println("New: " + newSaver.get("launcherVersion"));
            println("pas égal");
            infosLabel.setText("T\u00e9l\u00e9chargement de la mise \u00e0 jour");
            try {
                progressBar.setVisible(true);
                percentLabel.setVisible(true);
                bytesLabel.setVisible(true);
                downloadFromInternet(getJarLink(), launcherJarFile, progressBar, percentLabel, bytesLabel);
                println("jar downloaded");
                try {
                    copyFile(newPropertiesFile, currentPropertiesFile, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                progressBar.setVisible(false);
                percentLabel.setVisible(false);
                bytesLabel.setVisible(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            println("Dernière version détectée");
            infosLabel.setText("Derni\u00e8re version d\u00e9tect\u00e9e");
        }
    }

    static void update() {
        astrauworldFolder.mkdir();

        println("");
        println("---- JAVA 17 VERIF ----");

        infosLabel.setText("V\u00e9rification de Java 17");
        String javaHome = System.getenv("JAVA_HOME");
        String[] javaHomeSplit1 = javaHome.split(";");
        String pattern = Pattern.quote(separatorChar);
        String[] javaHomeSplit2 = javaHomeSplit1[0].split(pattern);
        String firstReferencedJavaVersion = javaHomeSplit2[javaHomeSplit2.length - 1];
        String[] javaHomeSplit3 = firstReferencedJavaVersion.split("\\.");
        String firstReferencedJavaGlobalVersion = javaHomeSplit3[0];
        println("%JAVA_HOME%: " + javaHome);
        println("First referenced java: " + javaHomeSplit1[0]);
        println("Last part of first referenced java: " + firstReferencedJavaVersion);
        println("First referenced java global version: " + firstReferencedJavaGlobalVersion);

        if (firstReferencedJavaGlobalVersion.contains("17")) {
            println("Java 17 détecté");
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

        updateJar();

    }

    static void launch() throws IOException {
        newPropertiesPath.toFile().delete();

        String[] parts = System.getenv("JAVA_HOME").split(";");
        String cmd = "\"" + parts[0] + separatorChar + "bin" + separatorChar + "java" + "\" -cp \"" + launcherJar + "\" fr.timeto.astrauworld.launcher.main.LauncherFrame";

        println("Commande: " + cmd);

        try {
            Runtime rt = Runtime.getRuntime();

            System.out.println();
            System.out.println();
            Process process = rt.exec(cmd);

            splash.stop();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        initFonts();

        oldCurrentPropertiesFile.delete();

        JPanel loadingSpinner = new LoadingSpinner();
        loadingSpinner.setBounds(0, 0, splash.getWidth(), splash.getHeight());
        splash.add(loadingSpinner);

        splash.setIconImage(Swinger.getResourceIgnorePath("/icon.png"));
        splash.setSize(346, 446);
        panel.setLayout(null);

        infosLabel.setBounds(34, 372, 278, 20);
        infosLabel.setForeground(Color.WHITE);
        infosLabel.setFont(kollektifBoldFont.deriveFont(16f));
        splash.add(infosLabel);

        progressBar.setBounds(0, 431, 346, 15);
        splash.add(progressBar);
        progressBar.setVisible(false);

        percentLabel.setBounds(8, 411, 334, 20);
        percentLabel.setForeground(Color.WHITE);
        percentLabel.setFont(infosLabel.getFont());
        splash.add(percentLabel);
        percentLabel.setVisible(false);

        bytesLabel.setBounds(8, 411, 334, 20);
        bytesLabel.setForeground(Color.WHITE);
        bytesLabel.setFont(infosLabel.getFont());
        splash.add(bytesLabel);
        bytesLabel.setVisible(false);

        splash.display();

        update();

        infosLabel.setText("Lancement...");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    /*    println("");
        println("---- LAUNCH ----");

        try {
            launch();
        } catch (IOException e) {
            errorMessage("Erreur", "Erreur au lancement  du launcher");
            throw new RuntimeException(e);
        } */

    }
}
