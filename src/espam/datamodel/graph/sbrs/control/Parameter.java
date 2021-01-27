package espam.datamodel.graph.sbrs.control;

import java.util.HashMap;
import java.util.Vector;

/**
 * A run-time adaptive parameter
 */
public class Parameter {
    public Parameter(ParameterName name) {
        this.name = name;
        this.parameterValues = new Vector();
    }

    public void addParameterValue(ParameterValue value){
        parameterValues.add(value);
    }

    public void addParameterValueDistinct(ParameterValue value){
        if (!parameterValues.contains(value))
            parameterValues.add(value);
    }

    public void printValues(){
        for (ParameterValue value: parameterValues) {
            value.printDetails();
            System.out.print("; ");
        }
    }

    public Vector<ParameterValue> getParameterValues() {
        return parameterValues;
    }

    ParameterName name;
    //List of all values, taken by the parameter during the
    //SBRS execution
    Vector<ParameterValue> parameterValues;
}