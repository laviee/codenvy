/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.builds;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsDouble;
import static java.lang.Math.round;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

/** @author dnochevnov@codenvy.com */
public class TestBuildsRunsMemoryUsagePerHour extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testGetValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.BUILDS_RUNS_MEMORY_USAGE_PER_HOUR);
        DoubleValueData d = getAsDouble(metric, Context.EMPTY);
        assertEquals(round(d.getAsDouble() * 10000), 542);
    }

    @Test
    public void testGetExpandedValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.BUILDS_RUNS_MEMORY_USAGE_PER_HOUR);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        Assert.assertEquals(m.size(), 2);
        assertEquals("{user/ws/project1={project_id=user/ws/project1}, user/ws/project2={project_id=user/ws/project2}}", m.toString());
    }

    private void prepareData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        builder.put(Parameters.LOG, initLogs().getAbsolutePath());


        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS_FINISHED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USED_TIME, MetricType.BUILDS_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.USED_TIME, builder.build());


        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS_FINISHED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USED_TIME, MetricType.RUNS_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.USED_TIME, builder.build());
    }

    private File initLogs() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("STOPPED-BY-USER", "1")
                                      .build());

        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("TIMEOUT", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("FINISHED-NORMALLY", "1")
                                      .build());

        return LogGenerator.generateLog(events);
    }
}
