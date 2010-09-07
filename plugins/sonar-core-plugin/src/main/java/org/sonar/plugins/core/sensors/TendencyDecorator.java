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
package org.sonar.plugins.core.sensors;

import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.time.DateUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.*;
import org.sonar.jpa.dao.MeasuresDao;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

@Phase(name = Phase.Name.POST)
public class TendencyDecorator implements Decorator {

  public static final String PROP_DAYS_DESCRIPTION = "Number of days the tendency should be calculated on.";

  private MeasuresDao measuresDao;
  private TimeMachine timeMachine;
  private TimeMachineQuery query;
  private TendencyAnalyser analyser;

  public TendencyDecorator(TimeMachine timeMachine, MeasuresDao measuresDao) {
    this.timeMachine = timeMachine;
    this.measuresDao = measuresDao;
    this.analyser = new TendencyAnalyser();
  }

  protected TendencyDecorator(TimeMachine timeMachine, TimeMachineQuery query, TendencyAnalyser analyser) {
    this.timeMachine = timeMachine;
    this.query = query;
    this.analyser = analyser;
  }

  protected TimeMachineQuery initQuery(Project project) {
    int days = project.getConfiguration().getInt(CoreProperties.CORE_TENDENCY_DEPTH_PROPERTY, CoreProperties.CORE_TENDENCY_DEPTH_DEFAULT_VALUE);

    List<Metric> metrics = new ArrayList<Metric>();
    for (Metric metric : measuresDao.getMetrics()) {
      if (metric.isNumericType()) {
        metrics.add(metric);
      }
    }

    query = new TimeMachineQuery(null) // resource is set after
        .setFrom(DateUtils.addDays(project.getAnalysisDate(), -days))
        .setToCurrentAnalysis(true)
        .setMetrics(metrics);
    return query;
  }

  protected TimeMachineQuery resetQuery(Project project, Resource resource) {
    if (query == null) {
      initQuery(project);
    }
    query.setResource(resource);
    return query;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return true;
  }

  public void decorate(Resource resource, DecoratorContext context) {
    if (shouldDecorateResource(resource)) {
      resetQuery(context.getProject(), resource);
      List<Object[]> fields = timeMachine.getMeasuresFields(query);
      ArrayListMultimap<Metric, Double> valuesPerMetric = ArrayListMultimap.create();
      for (Object[] field : fields) {
        valuesPerMetric.put((Metric) field[1], (Double) field[2]);
      }

      for (Metric metric : query.getMetrics()) {
        Measure measure = context.getMeasure(metric);
        if (measure != null) {
          List<Double> values = valuesPerMetric.get(metric);
          values.add(measure.getValue());

          measure.setTendency(analyser.analyseLevel(valuesPerMetric.get(metric)));
          context.saveMeasure(measure);
        }
      }
    }
  }

  private boolean shouldDecorateResource(Resource resource) {
    return ResourceUtils.isSet(resource) || ResourceUtils.isSpace(resource);
  }
}