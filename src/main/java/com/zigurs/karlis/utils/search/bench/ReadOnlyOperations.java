package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import com.zigurs.karlis.utils.search.QuickSearch.MergePolicy;
import com.zigurs.karlis.utils.search.QuickSearch.UnmatchedPolicy;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.zigurs.karlis.utils.search.bench.CommonParams.BACKTRACKING;
import static com.zigurs.karlis.utils.search.bench.CommonParams.FALSE;
import static com.zigurs.karlis.utils.search.bench.CommonParams.IGNORE;
import static com.zigurs.karlis.utils.search.bench.CommonParams.INTERSECTION;
import static com.zigurs.karlis.utils.search.bench.CommonParams.TRUE;
import static com.zigurs.karlis.utils.search.bench.CommonParams.UNION;
import static com.zigurs.karlis.utils.search.bench.CommonParams.USA_STATES;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = CommonParams.FORKS, jvmArgsAppend = {"-XX:+UseParallelGC", "-Xms4g", "-Xmx4g"})
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class ReadOnlyOperations {

    @State(Scope.Benchmark)
    public static class SearchWrapper {
        @Param({UNION, INTERSECTION})
        private MergePolicy merge;

        @Param({IGNORE, BACKTRACKING})
        private UnmatchedPolicy unmatched;

        @Param({"", "nosuchstring", "washington", "wa sh", "a b c d e", "a b c d e g h i l m n p r s u v y"})
        private String query;

        @Param({TRUE, FALSE})
        private boolean parallel;

        @Param({TRUE, FALSE})
        private boolean intern;

        private QuickSearch<String> searchInstance;

        @Setup
        public void setup() {
            searchInstance = QuickSearch.builder()
                    .withMergePolicy(merge)
                    .withUnmatchedPolicy(unmatched)
                    .withParallelProcessing(parallel)
                    .withKeywordsInterning(intern)
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
    public int single(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.query, 10).size();
    }

    @Threads(8)
    @Benchmark
    public int multi(SearchWrapper wrapper) {
        return wrapper.searchInstance.findItems(wrapper.query, 10).size();
    }
}
