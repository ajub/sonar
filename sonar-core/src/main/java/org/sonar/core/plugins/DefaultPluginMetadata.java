/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.core.plugins;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sonar.api.platform.PluginMetadata;

import java.io.File;
import java.util.List;

public class DefaultPluginMetadata implements PluginMetadata, Comparable<PluginMetadata> {
  private File file;
  private List<File> deployedFiles = Lists.newArrayList();
  private List<File> deprecatedExtensions = Lists.newArrayList();
  private String[] pathsToInternalDeps = new String[0];
  private String key;
  private String version;
  private String name;
  private String mainClass;
  private String description;
  private String organization;
  private String organizationUrl;
  private String license;
  private String homepage;
  private boolean useChildFirstClassLoader;
  private String basePlugin;
  private boolean core;

  private DefaultPluginMetadata() {
  }

  public static DefaultPluginMetadata create(File file) {
    return new DefaultPluginMetadata().setFile(file);
  }

  public File getFile() {
    return file;
  }

  public DefaultPluginMetadata setFile(File file) {
    this.file = file;
    return this;
  }

  public List<File> getDeployedFiles() {
    return deployedFiles;
  }

  public DefaultPluginMetadata addDeployedFile(File f) {
    this.deployedFiles.add(f);
    return this;
  }

  public List<File> getDeprecatedExtensions() {
    return deprecatedExtensions;
  }

  public DefaultPluginMetadata addDeprecatedExtension(File f) {
    this.deprecatedExtensions.add(f);
    return this;
  }

  public DefaultPluginMetadata setDeprecatedExtensions(List<File> files) {
    this.deprecatedExtensions = (files==null ? Lists.<File>newArrayList() : files);
    return this;
  }

  public String[] getPathsToInternalDeps() {
    return pathsToInternalDeps;
  }

  public DefaultPluginMetadata setPathsToInternalDeps(String[] pathsToInternalDeps) {
    this.pathsToInternalDeps = pathsToInternalDeps;
    return this;
  }

  public String getKey() {
    return key;
  }

  public DefaultPluginMetadata setKey(String key) {
    this.key = key;
    return this;
  }

  public String getName() {
    return name;
  }

  public DefaultPluginMetadata setName(String name) {
    this.name = name;
    return this;
  }

  public String getMainClass() {
    return mainClass;
  }

  public DefaultPluginMetadata setMainClass(String mainClass) {
    this.mainClass = mainClass;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public DefaultPluginMetadata setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getOrganization() {
    return organization;
  }

  public DefaultPluginMetadata setOrganization(String organization) {
    this.organization = organization;
    return this;
  }

  public String getOrganizationUrl() {
    return organizationUrl;
  }

  public DefaultPluginMetadata setOrganizationUrl(String organizationUrl) {
    this.organizationUrl = organizationUrl;
    return this;
  }

  public String getLicense() {
    return license;
  }

  public DefaultPluginMetadata setLicense(String license) {
    this.license = license;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public DefaultPluginMetadata setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getHomepage() {
    return homepage;
  }

  public DefaultPluginMetadata setHomepage(String homepage) {
    this.homepage = homepage;
    return this;
  }

  public boolean hasKey() {
    return StringUtils.isNotBlank(key);
  }

  public boolean hasMainClass() {
    return StringUtils.isNotBlank(mainClass);
  }

  public DefaultPluginMetadata setUseChildFirstClassLoader(boolean use) {
    this.useChildFirstClassLoader = use;
    return this;
  }

  public boolean isUseChildFirstClassLoader() {
    return useChildFirstClassLoader;
  }

  public DefaultPluginMetadata setBasePlugin(String key) {
    this.basePlugin = key;
    return this;
  }

  public String getBasePlugin() {
    return basePlugin;
  }

  public boolean isCore() {
    return core;
  }

  public DefaultPluginMetadata setCore(boolean b) {
    this.core = b;
    return this;
  }

  public boolean isOldManifest() {
    return !hasKey() && hasMainClass();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultPluginMetadata that = (DefaultPluginMetadata) o;
    return !(key != null ? !key.equals(that.key) : that.key != null);

  }

  @Override
  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("key", key)
        .append("version", StringUtils.defaultIfEmpty(version, "-"))
        .toString();
  }

  public int compareTo(PluginMetadata other) {
    return name.compareTo(other.getName());
  }
}
