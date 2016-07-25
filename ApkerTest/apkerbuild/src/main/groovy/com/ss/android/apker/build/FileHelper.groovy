package com.ss.android.apker.build

import com.android.builder.model.BuildType
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * Created by chenlifeng on 16/6/14.
 */
public class FileHelper {
    public static CLASSES_PATH = '/intermediates/classes/'

    public static String getBuildClassPath(Project project, BuildType type){
        project.buildDir.path + CLASSES_PATH + type.name
    }

    public static String getBuildManifestsPath(Project project, BuildType type) {
        project.buildDir.path + "/intermediates/manifests/full/" + type.name
    }

    public static String getDependceLibClassPath(Project project) {
        //TODO pack with jar and pack in apkerbuild or maven
        "/Users/chenlifeng/work/code/apker/ApkerTest/apker/build" + CLASSES_PATH + "release"
    }

    public static boolean isJar(String path){
        path.endsWith('.jar')
    }

    public static FileCollection getClassFiles(Project project, String path) {
        def fileTree = project.fileTree([dir: path, include: "**/*.class"])
        fileTree.filter { File file ->
            !file.name.equals("R.class") && !file.name.contains('R$') && !file.name.equals('BuildConfig.class')
        }
    }

    public static FileCollection getClassFileInJar(Project project, String path){
        def tree =   project.zipTree(path)
        tree.filter { File file ->
            !file.name.equals('BuildConfig.class') && file.name.endsWith('.class')
        }
    }

    public static String fileNameToClassName(String fileName){
        def s = fileName.replace('/', '.')
        s.replace('.class' , '')
    }
}