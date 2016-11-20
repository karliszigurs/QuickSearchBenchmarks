package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import com.zigurs.karlis.utils.search.QuickSearch.MergePolicy;
import com.zigurs.karlis.utils.search.QuickSearch.UnmatchedPolicy;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.zigurs.karlis.utils.search.bench.CommonParams.USA_STATES;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class ReadOnlyOperations {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        @Param({"UNION", "INTERSECTION"})
        private MergePolicy mergePolicy;

        @Param({"IGNORE", "BACKTRACKING"})
        private UnmatchedPolicy unmatchedPolicy;

        @Param({"", "nosuchstring", "washington", "wa sh", "a b c d e", "a b c d e g h i l m n p r s u v y"})
        private String searchString;

        @Param({"true", "false"})
        private boolean enableParallel = false;

        @Param({"true", "false"})
        private boolean enableInterning = false;

        private QuickSearch<String> searchInstance;

        @Setup
        public void setup() {
            this.searchInstance = QuickSearch.builder()
                    .withMergePolicy(mergePolicy)
                    .withUnmatchedPolicy(unmatchedPolicy)
                    .withParallelProcessing(enableParallel)
                    .withKeywordsInterning(enableInterning)
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

    @Threads(1)
    @Benchmark
    public int run_st(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.searchString, 10).size();
    }

    @Threads(8)
    @Benchmark
    public int run_mt(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.searchString, 10).size();
    }
}
