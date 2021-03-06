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
package org.sonar.batch.bootstrap;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.platform.PluginMetadata;
import org.sonar.api.platform.PluginRepository;
import org.sonar.core.plugins.PluginClassloaders;
import org.sonar.core.plugins.PluginFileExtractor;
import org.sonar.core.plugins.RemotePlugin;

import java.io.File;
import java.util.*;

public class BatchPluginRepository implements PluginRepository {

  private static final Logger LOG = LoggerFactory.getLogger(BatchPluginRepository.class);
  private static final String CORE_PLUGIN = "core";

  private ArtifactDownloader artifactDownloader;
  private Map<String, Plugin> pluginsByKey;
  private Map<String, PluginMetadata> metadataByKey;
  private Set<String> whiteList = null;
  private Set<String> blackList = null;
  private PluginClassloaders classLoaders;

  public BatchPluginRepository(ArtifactDownloader artifactDownloader, Configuration configuration) {
    this.artifactDownloader = artifactDownloader;
    if (configuration.getString(CoreProperties.BATCH_INCLUDE_PLUGINS) != null) {
      whiteList = Sets.newTreeSet(Arrays.asList(configuration.getStringArray(CoreProperties.BATCH_INCLUDE_PLUGINS)));
      LOG.info("Include plugins: " + Joiner.on(", ").join(whiteList));
    }
    if (configuration.getString(CoreProperties.BATCH_EXCLUDE_PLUGINS) != null) {
      blackList = Sets.newTreeSet(Arrays.asList(configuration.getStringArray(CoreProperties.BATCH_EXCLUDE_PLUGINS)));
      LOG.info("Exclude plugins: " + Joiner.on(", ").join(blackList));
    }
    // TODO reactivate somewhere else:  LOG.info("Execution environment: {} {}", environment.getKey(), environment.getVersion());
  }

  public void start() {
    doStart(artifactDownloader.downloadPluginIndex());
  }

  void doStart(List<RemotePlugin> remotePlugins) {
    PluginFileExtractor extractor = new PluginFileExtractor();
    metadataByKey = Maps.newHashMap();
    for (RemotePlugin remote : remotePlugins) {
      if (isAccepted(remote.getKey())) {
        List<File> pluginFiles = artifactDownloader.downloadPlugin(remote);
        List<File> extensionFiles = pluginFiles.subList(1, pluginFiles.size());
        PluginMetadata metadata = extractor.installInSameLocation(pluginFiles.get(0), remote.isCore(), extensionFiles);
        if (StringUtils.isBlank(metadata.getBasePlugin()) || isAccepted(metadata.getBasePlugin())) {
          // TODO log when excluding plugin
          metadataByKey.put(metadata.getKey(), metadata);
        }
      }
    }
    classLoaders = new PluginClassloaders(Thread.currentThread().getContextClassLoader());
    pluginsByKey = classLoaders.init(metadataByKey.values());
  }

  public void stop() {
    if (classLoaders != null) {
      classLoaders.clean();
      classLoaders = null;
    }
  }

  public Collection<Plugin> getPlugins() {
    return pluginsByKey.values();
  }

  public Plugin getPlugin(String key) {
    return pluginsByKey.get(key);
  }

  public Map<String, Plugin> getPluginsByKey() {
    return Collections.unmodifiableMap(pluginsByKey);
  }

  // TODO remove this method. Not used in batch.
  public Property[] getProperties(Plugin plugin) {
    if (plugin != null) {
      Class<? extends Plugin> classInstance = plugin.getClass();
      if (classInstance.isAnnotationPresent(Properties.class)) {
        return classInstance.getAnnotation(Properties.class).value();
      }
    }
    return new Property[0];
  }

  public Collection<PluginMetadata> getMetadata() {
    return metadataByKey.values();
  }

  public PluginMetadata getMetadata(String pluginKey) {
    return metadataByKey.get(pluginKey);
  }

  boolean isAccepted(String pluginKey) {
    if (CORE_PLUGIN.equals(pluginKey)) {
      return true;
    }
    if (whiteList != null) {
      return whiteList.contains(pluginKey);
    }
    return blackList == null || !blackList.contains(pluginKey);
  }
}
