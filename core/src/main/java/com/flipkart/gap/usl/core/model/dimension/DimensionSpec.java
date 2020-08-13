package com.flipkart.gap.usl.core.model.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Objects of this class are used to store info regarding external to internal Event mappings.
 */
@Getter
@Setter
@AllArgsConstructor
public class DimensionSpec implements Serializable {
    private Class<? extends Dimension> dimensionClass;
    private String name;
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass().getCanonicalName() != o.getClass().getCanonicalName()) return false;

        DimensionSpec that = (DimensionSpec) o;

        if (version != that.version) return false;
        if (dimensionClass != null ? !dimensionClass.getCanonicalName().equals(that.dimensionClass.getCanonicalName()) : that.dimensionClass != null)
            return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = dimensionClass != null ? dimensionClass.getCanonicalName().hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + version;
        return result;
    }
}
