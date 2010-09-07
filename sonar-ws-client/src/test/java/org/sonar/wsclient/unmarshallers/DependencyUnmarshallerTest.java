/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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
package org.sonar.wsclient.unmarshallers;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonar.wsclient.services.Dependency;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DependencyUnmarshallerTest {

  @Test
  public void toModel() throws IOException {
    Dependency dependency = new DependencyUnmarshaller().toModel("[]");
    assertThat(dependency, nullValue());

    dependency = new DependencyUnmarshaller().toModel(loadFile("/dependencies/single.json"));
    assertThat(dependency.getId(), is("1649"));
    assertThat(dependency.getFromKey(), is("org.apache.shiro:shiro-core:org.apache.shiro.authc.pam"));
    assertThat(dependency.getToKey(), is("org.apache.shiro:shiro-core:org.apache.shiro.realm"));
    assertThat(dependency.getUsage(), is("USES"));
    assertThat(dependency.getWeight(), is(5));
    assertThat(dependency.getFromName(), is("pam"));
    assertThat(dependency.getToName(), is("realm"));
    assertThat(dependency.getFromQualifier(), is("PAC"));
    assertThat(dependency.getToQualifier(), is("PAC"));
  }

  @Test
  public void toModels() throws IOException {
    Collection<Dependency> dependencies = new DependencyUnmarshaller().toModels("[]");
    assertThat(dependencies.size(), is(0));

    dependencies = new DependencyUnmarshaller().toModels(loadFile("/dependencies/many.json"));
    assertThat(dependencies.size(), is(15));
  }

  private static String loadFile(String path) throws IOException {
    return IOUtils.toString(DependencyUnmarshallerTest.class.getResourceAsStream(path));
  }

}