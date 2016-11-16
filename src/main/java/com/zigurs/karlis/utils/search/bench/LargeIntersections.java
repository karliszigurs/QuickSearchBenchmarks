package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import com.zigurs.karlis.utils.search.QuickSearch.AccumulationPolicy;
import com.zigurs.karlis.utils.search.QuickSearch.UnmatchedPolicy;
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

    @Benchmark
    public int run(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.searchString, 10).size();
    }

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        @Param({"UNION", "INTERSECTION"})
        private AccumulationPolicy accumulationPolicy;

        @Param({"EXACT", "BACKTRACKING"})
        private UnmatchedPolicy unmatchedPolicy;

        @Param({"wa sh", "a b c d e", "a b c d e g h i l m n p r s u v y"})
        private String searchString;

        @Param({"true", "false"})
        private boolean fjEnabled = false;

        private QuickSearch<String> searchInstance;

        public SearchWrapper() {
        }

        @Setup
        public void setup() {
            if (fjEnabled)
                this.searchInstance = QuickSearch.builder()
                        .withAccumulationPolicy(accumulationPolicy)
                        .withUnmatchedPolicy(unmatchedPolicy)
                        .withForkJoinProcessing()
                        .build();

            else
                this.searchInstance = QuickSearch.builder()
                        .withAccumulationPolicy(accumulationPolicy)
                        .withUnmatchedPolicy(unmatchedPolicy)
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
}
