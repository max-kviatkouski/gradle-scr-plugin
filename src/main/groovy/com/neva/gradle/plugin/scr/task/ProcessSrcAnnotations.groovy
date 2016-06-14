package com.neva.gradle.plugin.scr.task

import org.apache.felix.scrplugin.ant.SCRDescriptorTask
import org.apache.tools.ant.types.Path
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

public class ProcessSrcAnnotations extends DefaultTask {

    static final String MAIN_TASK_NAME = 'processScrAnnotations'

    static final String TEST_TASK_NAME = 'processTestScrAnnotations'

    @Input
    String sourceSetName

    @OutputDirectory
    File getOutput() {
        return new File(findSourceSet(project).output.classesDir, 'OSGI-INF')
    }

    public PackageAction() {
        group = 'Build'
        description = 'Scans classes to find annotated with the Felix SCR service annotations and creates proper xml files required by OSGI (in OSGI-INF directory).'
    }

    public void configure(String sourceSetName) {
        this.sourceSetName = sourceSetName

        def files = project.fileTree(dir: findSourceSet(project).output.classesDir, exclude: '**/OSGI-INF/**')
        inputs.files(files)
    }

    @TaskAction
    def run() {
        final SourceSet sourceSet = findSourceSet(project)

        final classesDir = sourceSet.output.classesDir

        project.logger.info "Running SCR for ${classesDir}"
        if (classesDir.exists()) {
            final antProject = project.ant.project
            final runtimePath = sourceSet.runtimeClasspath.asPath

            new SCRDescriptorTask(
                    srcdir: classesDir,
                    destdir: classesDir,
                    classpath: new Path(antProject, runtimePath),
                    strictMode: false,
                    project: antProject,
                    scanClasses: true).execute()
        }
    }

    private SourceSet findSourceSet(Project project) {
        project.convention.findPlugin(JavaPluginConvention)?.sourceSets?.getByName(sourceSetName)
    }
}