package com.zigurs.karlis.utils.search.bench;

import com.zigurs.karlis.utils.search.QuickSearch;
import com.zigurs.karlis.utils.search.QuickSearch.ACCUMULATION_POLICY;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.zigurs.karlis.utils.search.bench.CommonParams.USA_STATES;

@Threads(8)
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class ReadOnlyOperations {

    @Param({"0", "1024", "-1"})
    private int cacheSize;

    @Param({"UNION", "INTERSECTION"})
    private ACCUMULATION_POLICY accumulationPolicy;

    @Param({"", "nosuchstring", "washington", "wa sh", "a b c d e", "a b c d e g h i l m n p r s u v y"})
    private String searchString;

    private QuickSearch<String> searchInstance;

    @Setup
    public void setup() {
        this.searchInstance = QuickSearch.builder()
                .withAccumulationPolicy(accumulationPolicy)
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

    @GroupThreads(8)
    @Benchmark
    public int run() {
        return searchInstance.findItems(searchString, 10).size();
    }
}
