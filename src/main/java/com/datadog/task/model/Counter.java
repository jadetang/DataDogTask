package com.datadog.task.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A class count the frequency of each item.
 */
public class Counter {

    private final Map<String, Integer> counter;

    public Counter() {
        this.counter = new HashMap<>();
    }

    public void increase(String item) {
        if (item == null) {
            return;
        }
        increase(item, 1);
    }

    private void increase(String item, int count) {
        counter.put(item, counter.getOrDefault(item, 0) + count);
    }

    public Map<String, Integer> getAllItemCounts() {
        return Collections.unmodifiableMap(counter);
    }

    public void merge(Counter other) {
        other.getAllItemCounts().forEach(this::increase);
    }

    /**
     * Return top k items with highest frequencies. If there is a tie, sort the items with alphabetical order. If the
     * number of items is less than k, return all items.
     *
     * @param k top k
     * @return A sorted list of SimpleEntry<String, Integer>
     */
    public List<Entry<String, Integer>> topK(int k) {
        return counter.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()) == 0 ? e1.getKey().compareTo(e2.getKey()) :
                        e2.getValue().compareTo(e1.getValue())).limit(k).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return counter.toString();
    }
}
