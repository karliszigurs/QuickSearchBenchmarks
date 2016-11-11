package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import com.zigurs.karlis.utils.search.QuickSearch.ACCUMULATION_POLICY;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.zigurs.karlis.utils.search.bench.CommonParams.USA_STATES;

@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class LargeIntersections {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        @Param({"0", "1024", "104857600", "-1"})
        private int cacheSize;

        @Param({"UNION", "INTERSECTION"})
        private ACCUMULATION_POLICY accumulationPolicy;

        @Param({"EXACT", "BACKTRACKING"})
        private QuickSearch.UNMATCHED_POLICY unmatchedPolicy;

        @Param({"wa sh", "a b c d e", "a b c d e g h i l m n p r s u v y"})
        private String searchString;

        @Param({"true", "false"})
        private boolean fjEnabled = false;

        private QuickSearch<String> searchInstance;

        @Setup
        public void setup() {
            if (fjEnabled)
                this.searchInstance = QuickSearch.builder()
                        .withAccumulationPolicy(accumulationPolicy)
                        .withUnmatchedPolicy(unmatchedPolicy)
                        .withCacheLimit(cacheSize)
                        .withForkJoinProcessing()
                        .build();

            else
                this.searchInstance = QuickSearch.builder()
                        .withAccumulationPolicy(accumulationPolicy)
                        .withUnmatchedPolicy(unmatchedPolicy)
                        .withCacheLimit(cacheSize)
                        .build();

            /* Populate search dataset */
            for (int i = 0; i < 1000; i++) {
                String[] items = USA_STATES[i % USA_STATES.length];
                String item = String.format("%s-%s", items[0], i);
                String keywords = String.format("%s %s %s %s", items[0], items[1], items[2], items[3]);

                if (!searchInstance.addItem(item, keywords))
                    throw new IllegalStateException("Failed to add item");
            }
        }
    }

    @Benchmark
    public int run(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.searchString, 10).size();
    }
}
