package com.ss.android.apker.build

import com.android.builder.model.BuildType
import groovy.io.FileType
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtMethod
import javassist.CtNewMethod
import org.gradle.api.Project
import org.gradle.internal.hash.HashUtil
/**
 * Created by chenlifeng on 16/6/14.
 */
public class ByteHelper {
    private static METHOD_attachBaseContext = "protected void attachBaseContext(android.content.Context newBase) {" +
            "newBase = com.ss.android.apker.helper.ContextBuild.build(newBase, this.getClass()); " +
            "super.attachBaseContext(newBase); " +
            "}"

    public static void injectWithActivity(Project project, BuildType type, String androidSdk) {
        String classPath = FileHelper.getBuildClassPath(project, type)
        //new ClassPool()
        ClassPool pool = ClassPool.getDefault()
        pool.appendClassPath(androidSdk + '/platforms/android-23/android.jar')
        println '#################path=' + FileHelper.getDependceLibClassPath(project)
        pool.appendClassPath(FileHelper.getDependceLibClassPath(project))
        def dir = new File(project.buildDir.path + '/intermediates/exploded-aar')
        if (dir.exists()) {
            dir.traverse(
                    type:FileType.FILES,
                    nameFilter:~/.*\.jar/
            ) {
                pool.appendClassPath(it.path)
            };
        }
        pool.insertClassPath(classPath)
        injectMethod(project, type, classPath, pool, METHOD_attachBaseContext)
    }

    private static void injectMethod(Project project, BuildType type, String classRootPath, ClassPool classes, String method) {
        def classFiles
        String parentPath
        if(FileHelper.isJar(classRootPath)){
            def jarPath = new File(project.buildDir.path + '/tmp/expandedArchives/' + 'classes.jar_' + HashUtil.createCompactMD5(classRootPath))
            if(!jarPath.exists()){
                classFiles = FileHelper.getClassFileInJar(project, classRootPath)
            } else {
                classFiles = FileHelper.getClassFiles(project, jarPath.path)
            }
            parentPath = jarPath.path
        }else {
            classFiles = FileHelper.getClassFiles(project, classRootPath)
            parentPath = classRootPath
        }

        File manifestFile = new File(FileHelper.getBuildManifestsPath(project, type), 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile)
        def packageName = manifest.@package
        def applicationName = manifest.application.'@android:name'
        if (applicationName.toString().startsWith('.')) {
            applicationName = packageName + applicationName;
        }
        println '#################111 applicationName=' + applicationName
        def activities = manifest.application.activity

        classFiles.each { File file ->
            def className = FileHelper.fileNameToClassName(file.absolutePath.replace(parentPath + '/', ''))
            def c = classes.getCtClass(className)
            if (c.isFrozen()) {
                c.defrost()
            }
            if(!c.interface && !c.annotation && !c.enum && !className.startsWith('android.support.')){
                def shouldInject = false
                for (activity in activities) {
                    if (activity.'@android:name' == className) {
                        shouldInject = true
                        break;
                    }
                }
                if (!shouldInject && applicationName == className) {
                    shouldInject = true;
                }
                if (shouldInject) {
                    println '**********************************************className=' + className
                    println '**********************************************method=' + method
                    try {
                        CtMethod newMethod = CtNewMethod.make(method, c)
                        println '**********************************************newMethod=' + newMethod
                        c.addMethod(newMethod)
                        c.writeFile(parentPath)
                    } catch (CannotCompileException e){
                        println 'error message: ' + e.reason
                    }
                }
            }
        }
    }
}