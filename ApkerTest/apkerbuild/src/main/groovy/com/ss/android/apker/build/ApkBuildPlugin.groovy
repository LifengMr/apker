package com.ss.android.apker.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.ApplicationVariant

/**
 * Created by chenlifeng on 16/6/14.
 */
public class ApkBuildPlugin implements Plugin<Project> {
    private static SDK_DIR = ''
    private static BUILD_TOOLS_VERSION = ''
    private static TARGET_SDK_VERSION = 0;

    @Override
    void apply(Project project) {
        project.android.applicationVariants.all {ApplicationVariant variant ->
            init(project, variant)
            performJavaByte(project, variant)
        }
    }

    private static void init(Project project, ApplicationVariant variant){
        SDK_DIR = project.android.sdkDirectory.absolutePath
        BUILD_TOOLS_VERSION = project.android.buildToolsVersion
        TARGET_SDK_VERSION = variant.mergedFlavor.targetSdkVersion.apiLevel
    }

    private void performJavaByte(Project project, ApplicationVariant variant) {
        variant.javaCompiler << {
            if (!variant.buildType.minifyEnabled) {
                ByteHelper.injectWithActivity(project, variant.buildType, SDK_DIR)
            }
        }
    }
}