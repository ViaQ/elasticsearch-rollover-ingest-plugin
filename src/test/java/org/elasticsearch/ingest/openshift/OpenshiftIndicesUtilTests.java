/*
 * Copyright 2021 Lukáš Vlček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.ingest.openshift;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OpenshiftIndicesUtilTests extends ESTestCase {

    public void testIndicesWithoutWriteIndex() {
        List<String> indices = OpenshiftIndicesUtil.getIndicesWithoutWriteAlias(Collections.emptyMap());
        assertTrue(indices.isEmpty());

        Map<String, AliasOrIndex> im = new TreeMap<>();
        im.put("index1", new AliasOrIndex.Index(createIndexMetaData("index1")));
        im.put("index2", new AliasOrIndex.Index(createIndexMetaData("index2",
                new AliasInfo("foo1", false),
                new AliasInfo("foo2", false))));

        indices = OpenshiftIndicesUtil.getIndicesWithoutWriteAlias(im);
        assertFalse(indices.isEmpty());
    }

    private IndexMetaData createIndexMetaData(String index, AliasInfo ... alias) {
        IndexMetaData.Builder imBuilder = IndexMetaData.builder(index)
                .settings(Settings.builder().put("index.version.created", Version.CURRENT)) //required(*);
                .numberOfShards(3)   //required
                .numberOfReplicas(1); //required
        // (*) setting of 'index.version.created' must be done before numberOfShards is called, or the test
        // fails: Throwable #1: java.lang.IllegalArgumentException: must specify numberOfShards for index [___]

        for (AliasInfo ai : alias) {
            imBuilder.putAlias(AliasMetaData.builder(ai.alias).writeIndex(ai.writeAlias).build());
        }

        return imBuilder.build();
    }

    private class AliasInfo {
        public final String alias;
        public final Boolean writeAlias;
        AliasInfo(String alias, Boolean writeAlias) {
            this.alias = alias;
            this.writeAlias = writeAlias;
        }
    }
}
