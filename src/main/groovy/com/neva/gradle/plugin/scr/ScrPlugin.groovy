package com.neva.gradle.plugin.scr

import com.neva.gradle.plugin.scr.task.AppendServicesToManifest
import com.neva.gradle.plugin.scr.task.ProcessSrcAnnotations
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet

class ScrPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def processScrAnnotations = project.tasks.create(
                name: ProcessSrcAnnotations.MAIN_TASK_NAME,
                type: ProcessSrcAnnotations,
                dependsOn: JavaPlugin.CLASSES_TASK_NAME) {

            configure(SourceSet.MAIN_SOURCE_SET_NAME)
        }
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn processScrAnnotations

        def processTestScrAnnotations = project.tasks.create(
                name: ProcessSrcAnnotations.TEST_TASK_NAME,
                type: ProcessSrcAnnotations,
                dependsOn: JavaPlugin.TEST_CLASSES_TASK_NAME) {

            configure(SourceSet.TEST_SOURCE_SET_NAME)
        }
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn processTestScrAnnotations

        /*
         * This task has to been run separately from "processScrAnnotations" task, because of the output and inputs.
         * Ant src descriptor task generates MANIFEST.MF, which does not contain Service-Component: line. It has to be
         * added every time application is build.
         */
        def appendServicesToManifest = project.tasks.create(AppendServicesToManifest.TASK_NAME, AppendServicesToManifest)
        appendServicesToManifest.dependsOn processScrAnnotations
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn appendServicesToManifest
    }
}