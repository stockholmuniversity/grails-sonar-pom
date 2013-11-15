/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.maven.model.Build
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import org.codehaus.groovy.grails.plugins.PluginInfo

eventCompileEnd = { kind ->
  def file = 'sonar.xml'
  def grailsAppName = metadata.'app.name'

  println "Building sonar pom '${file}' for ${grailsAppName}"

  PluginInfo pluginInfo = pluginSettings.getPluginInfo(basedir)

  // Get groupId, from a few different locations
  def pluginAttrs = pluginInfo?.attributes
  def groupId = null
  if (pluginAttrs instanceof Map) {
    groupId = pluginAttrs?.group ?: pluginAttrs?.groupId
  }
  groupId = groupId ?: metadata.'app.groupId' ?: buildConfig?.grails?.project?.groupId ?: config?.grails?.project?.groupId

  if (! groupId) {
    println "WARNING: No groupId configured for ${grailsAppName}, will use app.name=${grailsAppName} for groupId."
  }

  Model model = new Model();
  model.modelVersion ="4.0.0"
  model.name = grailsAppName
  model.packaging = 'pom'

  model.groupId = groupId ?: grailsAppName
  model.artifactId = grailsAppName
  model.version = metadata.'app.version' ?: pluginInfo.version

  def sourceDirs = [
          'src/groovy',
          'src/java',
          'grails-app/domain',
          'grails-app/services',
          'grails-app/controllers',
          'grails-app/taglib'
  ]
  model.build = new Build(sourceDirectory: sourceDirs.join(','))

  model.addProperty('sonar.language', 'grvy')
  model.addProperty('sonar.dynamicAnalysis', 'reuseReports')
  model.addProperty('sonar.cobertura.reportPath', 'target/test-reports/cobertura/coverage.xml')
  model.addProperty('sonar.junit.reportsPath', 'target/test-reports')

  new MavenXpp3Writer().write(new OutputStreamWriter(new FileOutputStream(new File(file))), model );
}
