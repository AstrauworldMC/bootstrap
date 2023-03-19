package fr.timeto.astrauworld.bootstrap;

import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.timeto.timutilslib.PopUpMessages;
import net.harawata.appdirs.AppDirsFactory;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;

import static fr.theshark34.swinger.Swinger.getTransparentWhite;
import static fr.timeto.timutilslib.CustomFonts.*;
import static fr.timeto.timutilslib.PopUpMessages.*;
import static fr.timeto.timutilslib.TimFilesUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Bootstrap {

    static String OS = System.getProperty("os.name");
    static final String version = "1.2.0"; // TODO Changer la version
    static boolean isException = false;

    static SplashScreen splash = new SplashScreen("Astrauworld Launcher", Swinger.getResourceIgnorePath("/splash.png"));
    static JPanel panel = splash.getContentPane();

    static String separatorChar = System.getProperty("file.separator");
    static String astrauworldDir =  AppDirsFactory.getInstance().getUserDataDir("Astrauworld Launcher", null, null, true);

    static String customJavaDir = astrauworldDir + File.separator + "java";
    static String currentPropertiesDir = astrauworldDir + separatorChar + "launcher.properties";
    static String oldCurrentPropertiesDir = astrauworldDir + File.separator + "currentLauncher.properties";
    static String newPropertiesDir = astrauworldDir + separatorChar + "newLauncher.properties";
    static String launcherJar = astrauworldDir + separatorChar + "launcher.jar";

    static File astrauworldFolder = new File(astrauworldDir);
    static File customJavaFolder = new File(customJavaDir);
    static File currentPropertiesFile = new File(currentPropertiesDir);
    static File oldCurrentPropertiesFile = new File(oldCurrentPropertiesDir);
    static File newPropertiesFile = new File(newPropertiesDir);
    static File launcherJarFile = new File(launcherJar);

    static Path currentPropertiesPath = Paths.get(currentPropertiesDir);
    static Path newPropertiesPath = Paths.get(newPropertiesDir);

    static Saver currentSaver = new Saver(currentPropertiesPath);
    static Saver newSaver = new Saver(newPropertiesPath);

    static JLabel infosLabel = new JLabel("Si vous voyez ca, c'est pas bien", SwingConstants.CENTER) {
        @Override
        public void setText(String text) {
            super.setText(parseUnicode(text));
        }
    };
    static SColoredBar progressBar = new SColoredBar(getTransparentWhite(25), new Color(227, 6, 19));
    static JLabel percentLabel = new JLabel("", SwingConstants.RIGHT);
    static JLabel bytesLabel = new JLabel("", SwingConstants.LEFT);

    static void println(String str) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "] [Astrauworld Bootstrap] " + str);
    }

    public static void setTaskbarIcon(Image image) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class taskbar = Class.forName("java.awt.Taskbar");
        Method getTaskbar = taskbar.getDeclaredMethod("getTaskbar");
        Object instance = getTaskbar.invoke(taskbar);
        Method setIconImage = instance.getClass().getDeclaredMethod("setIconImage", Image.class);
        setIconImage.invoke(instance, image);
    }

    public static String parseUnicode(String oldString) {
        return oldString
                .replaceAll("é", "\u00e9")
                .replaceAll("è", "\u00e8")
                .replaceAll("ê", "\u00ea")
                .replaceAll("É", "\u00c9")
                .replaceAll("È", "\u00c8")
                .replaceAll("Ê", "\u00ca")
                .replaceAll("à", "\u00e0")
                .replaceAll("á", "\u00e1")
                .replaceAll("â", "\u00e2")
                .replaceAll("À", "\u00c0")
                .replaceAll("Â", "\u00c2");
    }

    public static String unparseUnicode(String oldString) {
        return oldString
                .replaceAll("\u00e9", "é")
                .replaceAll("\u00e8", "è")
                .replaceAll("\u00ea", "ê")
                .replaceAll("\u00c9", "É")
                .replaceAll("\u00c8", "È")
                .replaceAll("\u00ca", "Ê")
                .replaceAll("\u00e0", "à")
                .replaceAll("\u00e1", "á")
                .replaceAll("\u00e2", "â")
                .replaceAll("\u00c0", "À")
                .replaceAll("\u00c2", "Â");
    }

    private static String convertStringArrayToString(String[] strArr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr)
            sb.append(str).append(delimiter);
        return sb.substring(0, sb.length() - 1);
    }

    static String getJarLink() {
        return "https://github.com/AstrauworldMC/launcher/releases/download/" + newSaver.get("launcherVersion") + "/launcher.jar";
    }

    static void setPropertiesFile() throws Exception {
        infosLabel.setText("V\u00e9rification de la derni\u00e8re version");

        try {
            newPropertiesFile.createNewFile();
        } catch (IOException ignored) {}



        try {
            downloadFromInternet("https://raw.githubusercontent.com/AstrauworldMC/launcher/main/src/main/resources/launcher.properties", newPropertiesFile);
        //     downloadFromInternet("https://raw.githubusercontent.com/AstrauworldMC/launcher/Beta2.3.0/src/main/resources/launcher.properties", newPropertiesFile);
        } catch (Exception e) {
            currentPropertiesFile.delete();
            newPropertiesFile.delete();
            launcherJarFile.delete();
            throw e;
        }

        if (currentSaver.get("launcherVersion") == null) {
            try {
                currentPropertiesFile.createNewFile();
                println("created");
            } catch (IOException ignored) {}
        }
    }

    static void verifyBootstrapVersion() throws Exception {
        if (!version.equals(newSaver.get("bootstrapVersion"))) {
            throw new Exception("Nouvelle version du bootstrap disponible");
        }

    }

    static void updateJar() throws Exception {
        println("");
        println("---- JAR UPDATE ----");

        infosLabel.setText("Mise \u00e0 jour du launcher");

        try {
            if (launcherJarFile.createNewFile()) {
                currentSaver.set("launcherVersion", "");
                println("jar created");
            }
        } catch (IOException ignored) {}

        println("Current: " + currentSaver.get("launcherVersion"));
        println("New: " + newSaver.get("launcherVersion"));
        if (!Objects.equals(currentSaver.get("launcherVersion"), newSaver.get("launcherVersion"))) {
            println("pas égal");
            infosLabel.setText("T\u00e9l\u00e9chargement de la mise \u00e0 jour");
            progressBar.setVisible(true);
            percentLabel.setVisible(true);
            bytesLabel.setVisible(true);
            launcherJarFile.createNewFile();
            try {
                downloadFromInternet(getJarLink(), launcherJarFile, progressBar, percentLabel, bytesLabel);
            } catch (Exception e) {
                currentPropertiesFile.delete();
                newPropertiesFile.delete();
                launcherJarFile.delete();
                throw e;
            }
            println("jar downloaded");
            copyFile(newPropertiesFile, currentPropertiesFile, true);
            progressBar.setVisible(false);
            percentLabel.setVisible(false);
            bytesLabel.setVisible(false);

        } else {
            println("Dernière version détectée");
            infosLabel.setText("Derni\u00e8re version d\u00e9tect\u00e9e");
        }
    }

    static void update() throws Exception {
        astrauworldFolder.mkdir();

        if (!isException) {
            setPropertiesFile();

            currentSaver.load();
            newSaver.load();
        }

        if (!isException) {
            verifyBootstrapVersion();
        }

        if (!isException) {
            customJavaFolder = getJava();
        }

        if (!isException) {
            updateJar();
        }

    }

    static File getJava() throws Exception {
        println("");
        println("---- JAVA 17 VERIF ----");

        infosLabel.setText("V\u00e9rification de Java 17");
        try {
            println("-- Vérification du %JAVA_HOME% --");
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
                return new File(javaHomeSplit1[0]);
            } else {
                throw new NoSuchElementException("Java 17 non trouvé dans %JAVA_HOME%");
            }


        } catch (Exception e) {
            File jre17 = new File(customJavaDir + File.separator + "jre-17");
            jre17.mkdirs();

            println("Aucun Java 17 dans %JAVA_HOME% détecté");

            if (OS.toLowerCase().contains("win")) {
                if (new File(jre17 + File.separator + "bin" + File.separator + "java.exe").exists()) {
                    return jre17;
                } else {

                    if (System.getProperty("sun.arch.data.model").equals("64")) {
                        File zip = new File(customJavaFolder + File.separator + "jre17.zip");
                        zip.createNewFile();
                        infosLabel.setText("Téléchargement de Java 17");
                        println("Téléchargement de Java 17 pour Windows 64bits");
                        progressBar.setVisible(true);
                        percentLabel.setVisible(true);
                        bytesLabel.setVisible(true);
                        downloadFromInternet(
                                "https://download.bell-sw.com/java/17.0.6+10/bellsoft-jre17.0.6+10-windows-amd64-full.zip",
                                zip,
                                progressBar,
                                percentLabel,
                                bytesLabel
                        );
                        progressBar.setVisible(false);
                        percentLabel.setVisible(false);
                        bytesLabel.setVisible(false);

                        final ZipUnArchiver ua = new ZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
                        ConsoleLoggerManager manager = new ConsoleLoggerManager();
                        manager.initialize();
                        ua.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
                        ua.setSourceFile(zip);
                        ua.setDestDirectory(customJavaFolder);
                        ua.extract();

                        zip.delete();

                        Files.move(Paths.get(customJavaFolder.getAbsolutePath() + File.separator + "jre-17.0.6-full"), Paths.get(jre17.getAbsolutePath()), REPLACE_EXISTING);

                        new File(jre17 + File.separator + "jre-17.0.6-full").delete();

                        return jre17;
                    } else if (System.getProperty("sun.arch.data.model").equals("32")) {
                        File zip = new File(jre17 + File.separator + "jre17.zip");
                        zip.createNewFile();
                        infosLabel.setText("Téléchargement de Java 17");
                        println("Téléchargement de Java 17 pour Windows 32bits");
                        progressBar.setVisible(true);
                        percentLabel.setVisible(true);
                        bytesLabel.setVisible(true);
                        downloadFromInternet(
                                "https://download.bell-sw.com/java/17.0.6+10/bellsoft-jre17.0.6+10-windows-i586-full.zip",
                                zip,
                                progressBar,
                                percentLabel,
                                bytesLabel
                        );
                        progressBar.setVisible(false);
                        percentLabel.setVisible(false);
                        bytesLabel.setVisible(false);

                        final ZipUnArchiver ua = new ZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
                        ConsoleLoggerManager manager = new ConsoleLoggerManager();
                        manager.initialize();
                        ua.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
                        ua.setSourceFile(zip);
                        ua.setDestDirectory(customJavaFolder);
                        ua.extract();

                        zip.delete();

                        Files.move(Paths.get(customJavaFolder.getAbsolutePath() + File.separator + "jre-17.0.6-full"), Paths.get(jre17.getAbsolutePath()), REPLACE_EXISTING);

                        new File(jre17 + File.separator + "jre-17.0.6-full").delete();

                        return jre17;
                    } else {
                        throw new RuntimeException("Erreur au téléchargement de Java 17");
                    }
                }

            } else if (OS.toLowerCase().contains("mac")) {
                if (new File(jre17 + File.separator + "bin" + File.separator + "java").exists()) {
                    return jre17;
                } else {
                    File zip = new File(jre17 + File.separator + "jre17.zip");
                    zip.createNewFile();
                    infosLabel.setText("Téléchargement de Java 17");
                    println("Téléchargement de Java 17 pour Mac");
                    progressBar.setVisible(true);
                    percentLabel.setVisible(true);
                    bytesLabel.setVisible(true);
                    downloadFromInternet(
                            "https://download.bell-sw.com/java/17.0.6+10/bellsoft-jre17.0.6+10-macos-amd64-full.zip",
                            zip,
                            progressBar,
                            percentLabel,
                            bytesLabel
                    );
                    progressBar.setVisible(false);
                    percentLabel.setVisible(false);
                    bytesLabel.setVisible(false);

                    final ZipUnArchiver ua = new ZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
                    ConsoleLoggerManager manager = new ConsoleLoggerManager();
                    manager.initialize();
                    ua.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
                    ua.setSourceFile(zip);
                    ua.setDestDirectory(customJavaFolder);
                    ua.extract();

                    zip.delete();

                    Files.move(Paths.get(customJavaFolder.getAbsolutePath() + File.separator + "jre-17.0.6-full.jre"), Paths.get(jre17.getAbsolutePath()), REPLACE_EXISTING);

                    new File(jre17 + File.separator + "jre-17.0.6-full.jre").delete();

                    return jre17;

                }
            } else if (OS.toLowerCase().contains("nix") || OS.toLowerCase().contains("nux") || OS.toLowerCase().contains("aix")) {

                if (new File(jre17 + File.separator + "bin" + File.separator + "java").exists()) {
                    return jre17;
                } else {

                    if (System.getProperty("sun.arch.data.model").equals("64")) {
                        File zip = new File(jre17 + File.separator + "jre17.tar.gz");
                        zip.createNewFile();
                        infosLabel.setText("Téléchargement de Java 17");
                        println("Téléchargement de Java 17 pour Unix 64bits");
                        progressBar.setVisible(true);
                        percentLabel.setVisible(true);
                        bytesLabel.setVisible(true);
                        downloadFromInternet(
                                "https://download.bell-sw.com/java/17.0.6+10/bellsoft-jre17.0.6+10-linux-amd64-full.tar.gz",
                                zip,
                                progressBar,
                                percentLabel,
                                bytesLabel
                        );
                        progressBar.setVisible(false);
                        percentLabel.setVisible(false);
                        bytesLabel.setVisible(false);

                        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
                        ConsoleLoggerManager manager = new ConsoleLoggerManager();
                        manager.initialize();
                        ua.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
                        ua.setSourceFile(zip);
                        ua.setDestDirectory(customJavaFolder);
                        ua.extract();

                        zip.delete();

                        Files.move(Paths.get(customJavaFolder.getAbsolutePath() + File.separator + "jre-17.0.6-full"), Paths.get(jre17.getAbsolutePath()), REPLACE_EXISTING);

                        new File(jre17 + File.separator + "jre-17.0.6-full").delete();

                        return jre17;
                    } else if (System.getProperty("sun.arch.data.model").equals("32")) {
                        File zip = new File(jre17 + File.separator + "jre17.tar.gz");
                        zip.createNewFile();
                        infosLabel.setText("Téléchargement de Java 17");
                        println("Téléchargement de Java 17 pour Unix 32bits");
                        progressBar.setVisible(true);
                        percentLabel.setVisible(true);
                        bytesLabel.setVisible(true);
                        downloadFromInternet(
                                "https://download.bell-sw.com/java/17.0.6+10/bellsoft-jre17.0.6+10-linux-i586.tar.gz",
                                zip,
                                progressBar,
                                percentLabel,
                                bytesLabel
                        );
                        progressBar.setVisible(false);
                        percentLabel.setVisible(false);
                        bytesLabel.setVisible(false);

                        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
                        ConsoleLoggerManager manager = new ConsoleLoggerManager();
                        manager.initialize();
                        ua.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
                        ua.setSourceFile(zip);
                        ua.setDestDirectory(customJavaFolder);
                        ua.extract();

                        zip.delete();

                        Files.move(Paths.get(customJavaFolder.getAbsolutePath() + File.separator + "jre-17.0.6-full"), Paths.get(jre17.getAbsolutePath()), REPLACE_EXISTING);

                        new File(jre17 + File.separator + "jre-17.0.6-full").delete();

                        return jre17;
                    } else {
                        throw new RuntimeException("Erreur au téléchargement de Java 17");
                    }
                }

            } else {
                Thread ok = new Thread(() -> {
                    System.exit(1);
                });
                errorMessage("Erreur", "Désolé, votre système d´exploitation (" + OS + ") n'est pas compatible", ok);
                println("OS non supporté");
                return null;
            }

        }
    }

    static void launch() throws Exception {

        println("");
        println("---- LAUNCH ----");

        newPropertiesPath.toFile().delete();

        String javaCommand;
        final Path java = Paths.get(String.valueOf(customJavaFolder), "bin", "java");
        if (OS.toLowerCase().contains("win"))
            javaCommand = "\"" + java + "\"";
        else javaCommand = java.toString();
        JavaUtil.setJavaCommand(javaCommand);

        ClasspathConstructor classpath = new ClasspathConstructor() {
            @Override
            public String make()
            {
                final StringBuilder classPath = new StringBuilder();

                for (int i = 0; i < this.files.size(); i++)
                    if (OS.toLowerCase().contains("win"))
                        classPath.append("\"" + files.get(i).toString() + "\"").append(i + 1 == files.size() ? "" : File.pathSeparator);
                    else classPath.append(files.get(i).toString()).append(i + 1 == files.size() ? "" : File.pathSeparator);

                return classPath.toString();
            }
        };
        classpath.add(Paths.get(launcherJarFile.getAbsolutePath()));

        ExternalLaunchProfile profile = new ExternalLaunchProfile("fr.timeto.astrauworld.launcher.main.Main", classpath.make());
        ExternalLauncher launcher = new ExternalLauncher(profile);

        Process process = launcher.launch();

        splash.stop();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        System.out.println();

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            initFonts();
            isException = false;

            if (OS.toLowerCase().contains("win")) {
                println("Windows OK");
            } else if (OS.toLowerCase().contains("mac")) {
                println("MacOS OK");
                setTaskbarIcon(Swinger.getResourceIgnorePath("/icon.png"));
            } else if (OS.toLowerCase().contains("nix") || OS.toLowerCase().contains("nux") || OS.toLowerCase().contains("aix")) {
                println("Unix OK");
            } else {
                Thread ok = new Thread(() -> {
                    System.exit(1);
                });
                errorMessage("Erreur", "Désolé, votre système d'exploitation (" + OS + ") n'est pas compatible", ok);
                println("OS non supporté");
            }

            println("Astrauworld Launcher dir: " + astrauworldDir);

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


            if (!isException) {
                update();
            }

            infosLabel.setText("Lancement...");

            if (!isException) {
                launch();
            }
        } catch (Exception e) {
            isException = true;
            if (e.getMessage().contains("Nouvelle version du bootstrap disponible")) {
                Thread t = new Thread(() -> {
                    try {
                        Desktop.getDesktop().browse(URI.create("https://github.com/AstrauworldMC/bootstrap/releases"));
                    } catch (IOException ignored) {}

                    System.exit(0);
                });
                PopUpMessages.normalMessage("Nouvelle version", "Une nouvelle version du bootstrap (" + newSaver.get("bootstrapVersion") + ") est disponible.", t);
            } else {
                Thread t = new Thread(() -> {
                    System.exit(1);
                });
                try {
                    String[] causeSplit1 = e.getCause().toString().split(":");
                    String[] causeSplit2 = causeSplit1[0].split("\\.");
                    PopUpMessages.errorMessage(causeSplit2[causeSplit2.length - 1], parseUnicode(e.getLocalizedMessage()), t);
                } catch (Exception ex) {
                    PopUpMessages.errorMessage("Erreur (non reconnue)", parseUnicode(e.getLocalizedMessage()), t);
                }
            }
            e.printStackTrace();
        }

    }
}
