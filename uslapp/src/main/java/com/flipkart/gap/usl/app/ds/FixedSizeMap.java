package com.flipkart.gap.usl.app.ds;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.Delegate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by vinay.lodha on 13/09/17.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY )
@JsonIgnoreProperties({"empty" , "full"})
public class FixedSizeMap<K,V> {
    private int maxCapacity;
    @Nonnull
    @Delegate(excludes = AddFunctions.class)
    private Map<K,V> elements;

    @JsonCreator
    public FixedSizeMap(@JsonProperty("maxCapacity") int maxCapacity,@JsonProperty("elements") @Nonnull Map<K, V> elements) {
        this.maxCapacity = maxCapacity;
        this.elements = elements;
    }

    private interface AddFunctions<K,V> {
        V put(K key, V value);
        void putAll(Map<? extends K, ? extends V> m);
        V putIfAbsent(K key, V value);
        V computeIfAbsent(K key, Function<? super K, ? extends V> value);
        V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
        V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);
    }

    public V put(K key, V value) {
        if (elements.size() < maxCapacity) {
            return elements.put(key, value);
        }
        throw new IllegalArgumentException("Map is full");
    }

    public boolean isFull() {
        return elements.size() >= maxCapacity;
    }

    public V replaceIfFull(K key, V value, Predicate<Map.Entry<K,V>> p) {
        if (isFull()) {
            Optional<K> replacement = elements.entrySet().stream()
                    .filter(p)
                    .map(Map.Entry::getKey)
                    .findAny();
            if (replacement.isPresent()) {
                remove(replacement.get());
                return put(key, value);
            }
            throw new IllegalArgumentException("No matching element found for the predicate");
        } else {
            return put(key, value);
        }
    }

}

