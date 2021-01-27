package espam.datamodel.graph.sbrs.control;

import espam.datamodel.graph.csdf.datasctructures.Tensor;

import java.util.*;

/**
 * A run-time adaptive parameter value
 * @param <T>
 */
public class ParameterValue<T> {
    T value;

    public ParameterValue(T value) {
        this.value = value;
    }

    public T getValue() { return value; }

    public void printDetails(){
        System.out.print(value);
    }
}
