/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing;

import com.graphhopper.routing.ch.EdgeBasedPrepareContractionHierarchies;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.TurnCostExtension;
import org.junit.Test;

import java.util.List;

public class DijkstraBidirectionEdgeCHTest extends AbstractRoutingAlgorithmTester {
    @Override
    protected CHGraph getGraph(GraphHopperStorage ghStorage, Weighting weighting) {
        return ghStorage.getGraph(CHGraph.class, weighting);
    }

    @Override
    protected GraphHopperStorage createGHStorage(
            EncodingManager em, List<? extends Weighting> weightings, boolean is3D) {
        return new GraphHopperStorage(weightings, new RAMDirectory(),
                em, is3D, true, new TurnCostExtension()).create(1000);
    }

    @Override
    public RoutingAlgorithmFactory createFactory(GraphHopperStorage ghStorage, AlgorithmOptions opts) {
        EdgeBasedPrepareContractionHierarchies ch = new EdgeBasedPrepareContractionHierarchies(
                ghStorage, getGraph(ghStorage, opts.getWeighting()), opts.getWeighting())
                .usingRandomContractionOrder();
        ch.doWork();
        return ch;
    }

    @Test
    public void testRekeyBugOfIntBinHeap() {
        //todo:
        // this test is still really slow with edge based contraction hierarchies and needs to be enabled as soon
        // as the performance has been optimized
    }

}
