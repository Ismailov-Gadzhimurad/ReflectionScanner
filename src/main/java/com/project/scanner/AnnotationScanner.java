package com.project.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.cloud.openfeign.FeignClient;



public class AnnotationScanner {

    public static void scan() throws IOException {


        Properties properties = new Properties();
        properties.load(new FileInputStream("C:\\Users\\user\\Downloads\\scanner\\src\\main\\resources\\config.properties"));
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys){

            String gitUrl = properties.getProperty(key);


            String folderPath = "C:\\Users\\user\\Desktop\\" + key;
            File localDirectory = new File(folderPath);
            if (!localDirectory.exists()) {
                localDirectory.mkdirs();
            }

            try (Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(folderPath + "/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build()) {



                Git git = new Git(repository);
                git.cloneRepository()
                        .setURI(gitUrl + ".git")
                        .setDirectory(localDirectory)
                        .call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            Class<? extends Annotation> annotationClass = FeignClient.class;

            String fullFolderPath = folderPath + "\\src\\main\\java\\com\\example\\" + key;

            String batCommand = "cd /d \"" + folderPath + "\" && gradle build";


            try (PrintWriter writer = new PrintWriter("build.bat")) {
                writer.println(batCommand);
            }
            List<Class<?>> annotatedClasses = scanPackageForAnnotation(fullFolderPath, annotationClass);
            if (!annotatedClasses.isEmpty()) {
                System.out.println("Классы с аннотацией " + annotationClass.getSimpleName() + ":");
                for (Class<?> clazz : annotatedClasses) {
                    System.out.println(clazz.getName());
                }
            } else {
                System.out.println("Классы с аннотацией " + annotationClass.getSimpleName() + " не найдены.");
            }
        }

    }

    public static List<Class<?>> scanPackageForAnnotation(String packageName, Class<? extends Annotation> annotationClass) {
        List<Class<?>> annotatedClasses = new ArrayList<>();

        try {
            Iterator<URL> resources = URLClassLoader.getSystemClassLoader().getResources(packageName.replace("\\", "/")).asIterator();
            while (resources.hasNext()) {
                URL resource = resources.next();
                File directory = new File(resource.getPath());
                if (directory.isDirectory()) {
                    scanDirectoryForAnnotation(directory, packageName, annotationClass, annotatedClasses);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return annotatedClasses;
    }

    private static void scanDirectoryForAnnotation(File directory, String packageName, Class<? extends Annotation> annotationClass, List<Class<?>> annotatedClasses) {
        // проверяет есть ли в классе определенная аннотация, и добавляет этот класс в список annotatedClasses
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    try {
                        String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            annotatedClasses.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    // Рекурсивно ищем в подкаталогах
                    scanDirectoryForAnnotation(file, packageName + "." + file.getName(), annotationClass, annotatedClasses);
                }
            }
        }
    }
}
