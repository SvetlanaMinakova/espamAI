package espam.datamodel.graph.csdf.datasctructures;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import espam.datamodel.EspamException;
import espam.visitor.CNNGraphVisitor;

import java.util.Arrays;
import java.util.Arrays.*;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * int  representation. Used for describing data formats
 */
public class Tensor implements Cloneable, Comparable<Tensor> {
    /**
     * Create Tensor with certain dimension sizes
     * @param shape
     */
    public Tensor (int ...shape){
        _shape=new Vector<Integer>();
    for (int dimSize: shape)
        _shape.add(dimSize);
    }

    /**
     * Copy the tensor
     * @param t tensor for copying
     */
    public Tensor (Tensor t) {
        this._shape=new Vector<Integer>();
        for (Integer dimsize: t.getShape())
            addDimension(dimsize);
    }
    
    /**
     *  Clone this Tensor
     *
     * @return  a reference to the Tensor.
     */
    @SuppressWarnings(value={"unchecked"})
    public Tensor clone() {
        try {
            Tensor newObj = (Tensor) super.clone();
            newObj.setShape((Vector<Integer>)_shape.clone());
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }

        /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
     public void accept(CNNGraphVisitor x) { x.visitComponent(this); }

    /** compareTo method overload*/

   public int compareTo(Tensor tensor)
   {
     int thisDim = this.getDimensionality();
     int tesorDim = tensor.getDimensionality();

     /** this < tensor*/
     if(thisDim < tesorDim)
      return -1;

     /** this == tensor*/
     if(thisDim==tesorDim)
         return 0;

     /**this > tensor*/
     return 1;
  }

    /**
     * Serialize tensor as operation parameter
     * @param tensor
     * @return
     */
    public static String toOpParam(Tensor tensor){
        if(tensor==null)
            return "";
        StringBuilder res = new StringBuilder("");
        int lastSeparatorId = tensor.getDimensionality()-1;
        int separatorId=0;
        for(Integer dim: tensor.getShape()){
            res.append(dim);
            if(separatorId<lastSeparatorId)
                res.append("_");
            separatorId++;
        }

        return res.toString();

    }

    /**
     * Set sizes of all dimensions inside of Tensor
     * @param shape sizes of all dimensions inside of Tensor
     */
    public void setShape(Vector<Integer> shape) {
        this._shape = shape;
    }

    /**
     * Return  sizes of all dimensions inside of Tensor
     * @return sizes of all dimensions inside of Tensor
     */
    public Vector<Integer> getShape() {
        return _shape;
    }

    /**
     * Get dimensionality of tensor
     * @return dimensionality of tensor
     */
    public int getDimensionality() {
        return _shape.size();
    }

    /**
     * Add new dimension
     * @param dimSize new dimension size
     */
    public void addDimension(int dimSize) {
        _shape.add(dimSize);
    }

    /**
     * Inserts dimension into tensor description if it is possible
     * @param pos position of new dimension
     * @param dimSize size of new dimension
     */
    public void insertDimension(int dimSize, int pos) {
        _shape.insertElementAt(dimSize,pos);
    }

    /**
     * Removes last dimension
     */
    public void removeDimension() {
    _shape.removeElementAt(getDimensionality()-1);
    }

        /**
     * Removes last dimension
     */
    public void removeDimension(int pos) {
    _shape.removeElementAt(pos);
    }


    /**
     * Returns subtensor of current tensor
     * @param startDim start dimension of the subtensor
     * @param endDim end dimension of the subtensor
     */
    public Tensor getSubTensor(int startDim, int endDim) {
        int maxSubTensorLen = Math.max(getDimensionality(),endDim);
        Tensor subTensor = new Tensor();
        for(int i=startDim;i<maxSubTensorLen;i++)
            subTensor.addDimension(_shape.elementAt(i));
        return subTensor;
    }

    /**
     * Returns subtensor of current tensor with default start dim=0
     * @param endDim end dimension of the subtensor
     */
    public Tensor getSubTensor(int endDim) {
        int minSubTensorLen = Math.min(getDimensionality(),endDim);
        Tensor subTensor = new Tensor();
        for(int i=0;i<minSubTensorLen;i++)
            subTensor.addDimension(_shape.elementAt(i));
        return subTensor;
    }

    /**
     * Returns the size of given dimension, if dimension exists in this Tensor and 0 otherwise
     * @param dim sequence number of dimension
     * @return the size of given dimension, if dimension exists in this Tensor and 0 otherwise
     */
    public int getDimSize(int dim) {
        if(dim<=getDimensionality())
            return _shape.get(dim);
        else
            return 0;
    }


        /**
     * Returns the size of given dimension, if dimension exists in this Tensor and 0 otherwise
     * @param dim sequence number of dimension
     * @return the size of given dimension, if dimension exists in this Tensor and 0 otherwise
     */
    public void setDimSize(int dim,Integer size) {

        if(dim<=getDimensionality())
            _shape.set(dim,size);
    }
    /**
     * Return description of a tensor
     * @return description of a tensor
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (Integer dimSize:_shape) {
            result.append(dimSize);
            result.append(",");
        }
        /**
         * remove last comma
         */
        result.deleteCharAt(result.length()-1);
        result.append("]");
        return result.toString();
    }

     /**
     * Checks if all the tensors in the list have the same dimensionality
     * @param tensors list of tensors
     * @return true if all the tensors in the list have the same dimensionality
     * and false otherwise
     */
    public static boolean isHaveSameDimensionality(Tensor ... tensors) {
        if(tensors.length==0)
            return true;

        Tensor prev = tensors[0];
        for(Tensor tensor:tensors) {

            if(prev.getDimensionality()!=tensor.getDimensionality())
                return false;
            prev=tensor;
        }
        return true;
    }

    /**
     * Checks if all the tensors in the list have the same dimensionality
     * @param tensors list of tensors
     * @return true if all the tensors in the list have the same dimensionality
     * and false otherwise
     */
    public static boolean isHaveSameDimensionality(Vector<Tensor> tensors) {
        Tensor prev = tensors.firstElement();
        for(Tensor tensor:tensors) {

            if(prev.getDimensionality()!=tensor.getDimensionality())
                return false;
            prev=tensor;
        }
        return true;
    }

      /**
     * Merge tensors
     * @param tensors list of tensors
     * @param dim dimension to merge
     * @return merged Tensor
     * @throws EspamException if an error occurs
     */
    public static Tensor merge(int dim,Tensor ... tensors) throws EspamException{
        Vector<Tensor> tensorVec = new Vector<>();
        for(Tensor tensor:tensors)
            tensorVec.add(tensor);

        return merge(tensorVec,dim);
    }

    /**
     * Merge tensors
     * @param tensors list of tensors
     * @param dim dimension to merge
     * @return merged Tensor
     * @throws EspamException if an error occurs
     */
    public static Tensor merge(Vector<Tensor> tensors, int dim) throws EspamException{
        if(Tensor.isMergeable(tensors,dim)){
               Tensor merged = new Tensor(Tensor.getMaxDimTensor(tensors));

            int mergedDimSize = 0;

            for(Tensor tensor:tensors){
                int dimSize = tensor.getDimSize(dim);
                /** align tensors by adding dummy 1-valued dimension*/
                if(dimSize==0)
                    mergedDimSize+=1;
                else
                    mergedDimSize+=dimSize;
            }

            merged.setDimSize(dim,mergedDimSize);
            return merged;
        }

        System.err.println("Tensors merge error. ");
        for(Tensor tensor: tensors)
            System.err.print(tensor+" ");
        System.err.println(" could not be merged ");
        throw new EspamException("Tensors merge error. ");
    }

      /**
     * Checks if all the tensors in the list could be merged by last dimension
     * @param tensors list of tensors
     * @param dim dimension
     * @return true if all the tensors in the list could be merged and false otherwise
     */
    public static boolean isMergeable(Vector<Tensor> tensors, int dim) {
        Tensor prev = tensors.firstElement();
        int prevDimensionality,curDimensionality;

        for(Tensor tensor:tensors) {
            prevDimensionality = prev.getDimensionality();
            curDimensionality = tensor.getDimensionality();

            if(prevDimensionality<dim || curDimensionality<dim)
                return false;

            /** check all dimensions except of one to be merged*/
            for (int i=0; i< Math.min(prevDimensionality,curDimensionality); i++) {
                if(i!=dim) {
                    if (prev.getDimSize(i) != tensor.getDimSize(i))
                        return false;
                }
            }
            prev=tensor;
        }
        return true;
    }

    /**
      * Compares Tensor with another object
      * @param obj Object to compare this Tensor with
      * @return true if Neuron is equal to the object and false otherwise
      */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
               return false;
           }

        Tensor otherTensor = (Tensor) obj;
        if(this.getDimensionality()!=otherTensor.getDimensionality())
          return false;

        for(int i=0;i<getDimensionality();i++) {
            if(getDimSize(i)!=otherTensor.getDimSize(i))
                return false;
        }

        return true;

       }

      /**
      * Compares two Tensors
      * @param t1 first tensor
      * @param t2 second tensor
      * @return true if Neuron is equal to the object and false otherwise
      */
    public static boolean isSame(Tensor t1,Tensor t2) {
        if(t1==null && t2==null)
            return true;

        if(t1==null || t2==null)
            return false;

        return t1.equals(t2);
    }

     public boolean notEqual(Tensor otherTensor) {
        return !equals(otherTensor);
    }

      /**
     * Checks if the tensors have same elements number
     * @param t1 first tensor
     * @param t2 second tensor
     * @return true, if the Tensor could be transformed to another tensor
     * and false otherwise
     */
    public static boolean isHaveSameElementsNumber(Tensor t1, Tensor t2) {
        if(t1==null || t2 == null)
            return false;
        return t1.getElementsNumber()==t2.getElementsNumber();
    }

    /**
     * Return max number of elements in Tensor
     * @return max number of elements in Tensor
     */
    public int getElementsNumber() {
        if(getDimensionality()==0)
            return 0;
        int elemsNumber = 1;
        for(Integer dim: getShape())
            elemsNumber*=dim;
        return elemsNumber;
    }

    /**
     * Merges two last dimensions of the tensor
     */
    public void mergeTwoLastDim() {
        if(getDimensionality()>2) {
            int dimensionality = getDimensionality();
            int mergedElemSize = getDimSize(dimensionality-1) * getDimSize(dimensionality - 2);
            setDimSize(dimensionality - 2, mergedElemSize);
            removeDimension();
        }
    }

    /**
     * Splits last dimension in sequence
     * @param unitsNum number of units in resulting sequence
     * @throws EspamException if an error occurs
     */
    public void splitLastDim(int unitsNum) throws EspamException{
        if(unitsNum==1)
            return;

        if(getDimensionality()==0)
            throw new EspamException("empty Tensor.");

        if(unitsNum<1)
            throw new EspamException("invalid units number.");

        double dLastDimSize = (double)getLastDimSize();
        double dUnitsNum = (double)unitsNum;
        if(dLastDimSize%dUnitsNum!=0)
            throw new EspamException("last dimension should be divisible by units number.");

        int splittedDim = getLastDimSize()/unitsNum;
        removeDimension();
        addDimension(splittedDim);
        addDimension(unitsNum);
    }


         /**
     * Merges list of tensors by smallest common dimension
     * @param tensors tensors to be merged
     * @return list of tensors represented as one tensor
     * @throws EspamException if tensors could not be merged
     */
    public static Tensor mergeToSequence(Tensor ... tensors) throws EspamException {
        Vector<Tensor> tensorVec = new Vector<>();
        for(Tensor tensor: tensors)
            tensorVec.add(tensor);

        return mergeToSequence(tensorVec);
    }

     /**
     * Merges list of tensors by smallest common dimension
     * @param tensors tensors to be merged
     * @return list of tensors represented as one tensor
     * @throws EspamException if tensors could not be merged
     */
    public static Tensor mergeToSequence(Vector<Tensor> tensors) throws EspamException {
        if(tensors.size()==0)
             return new Tensor();

        if(tensors.size()==1)
           return tensors.elementAt(0);

        int dim = getMaxDimTensor(tensors).getDimensionality()-1;

        if(isHaveSameDimensionality(tensors))
           return merge(tensors,dim);

        Vector<Tensor> alignedTensors = align(tensors);
        return merge(alignedTensors,dim);
    }


    /**
     * Align list of tensors to max dimension
     * @param tensors tensors to be aligned
     * @return list of tensors, aligned to one dimension by adding dummy 1s
     */
    public static Vector<Tensor> align(Vector<Tensor> tensors){
        Vector<Tensor> aligned = new Vector<>();
        int alignedSize = getMaxDimTensor(tensors).getDimensionality();

        for(Tensor tensor: tensors){
            if(isNullTensor(tensor))
                aligned.add(tensor);
            else {
                Tensor alignedTensor = new Tensor(tensor);
                for(int i=tensor.getDimensionality();i<alignedSize;i++){
                    alignedTensor.addDimension(1);
                }
                aligned.add(alignedTensor);
            }
        }

        return aligned;
    }


    /**
     * Merges list of tensors into one large tensor by specified dimension
     * @param tensors tensors to be merged
     * @dim dimension to merge
     * @return list of tensors represented as one tensor
     * @throws EspamException if tensors could not be merged
     */
    public static Tensor mergeToSequence(Vector<Tensor> tensors, int dim) throws EspamException {
        if(tensors.size()==0)
             return new Tensor();

        if(tensors.size()==1)
           return tensors.elementAt(0);

        return merge(tensors,dim);
    }

    /** reverse Tensor arguments
     * @param tensor tensor to reverse
     * @return reversed tensor
     */
    public static Tensor reverse(Tensor tensor){
        if(tensor==null)
            return null;
        if(tensor.getDimensionality()==0)
            return tensor;

        Tensor reversed = new Tensor();
        for(int i=tensor.getDimensionality()-1;i>=0;i--)
            reversed.addDimension(tensor.getDimSize(i));

        return reversed;
    }

    /** checks if the Tensor is null-Tensor
     * @param tensor Tensor to be checked
     * @return true if Tensor is null and false otherwise
     */
    public static boolean isNullTensor(Tensor tensor){
        if(tensor==null)
            return true;
        return false;
    }

      /** checks if the Tensor is Null or Empty
     * @param tensor Tensor to be checked
     * @return true if Tensor is null and false otherwise
     */
    public static boolean isNullOrEmpty(Tensor tensor){
        if(tensor==null)
            return true;
        if(tensor.getDimensionality()==0)
            return true;
        return false;
    }

    /**Checks if the Tensor is Zero or Negative values
     * @param tensor Tensor to be checked
     * @return true if the Tensor is Zero or Negative values
     * and false otherwise*/
    public static boolean containsZeroOrNegativeDims(Tensor tensor){
        for(Integer val: tensor.getShape()){
            if(val<1)
                return true;
        }
        return false;
    }

    /** get Tensor with max dimensionality
     * @param tensors list of tensors to inspect
     * @return Tensor with max dimensionality or empty Tensor
     */
    public static Tensor getMaxDimTensor(Tensor... tensors){
        Tensor result = new Tensor();
        for (Tensor tensor: tensors){
            if(!isNullTensor(tensor)){
                if(tensor.getDimensionality()>result.getDimensionality())
                    result = tensor;
            }
        }

        return result;
    }

       /** get Tensor with max dimensionality
     * @param tensors list of tensors to inspect
     * @return Tensor with max dimensionality or empty Tensor
     */
    public static Tensor getMaxDimTensor(Vector<Tensor> tensors){
        Tensor result = new Tensor();
        for (Tensor tensor: tensors){
            if(!isNullTensor(tensor)){
                if(tensor.getDimensionality()>result.getDimensionality())
                    result = tensor;
            }
        }

        return result;
    }

     /** get Tensor with max dimensionality
     * @param tensors list of tensors to inspect
     * @return Tensor with max dimensionality or empty Tensor
     */
    public static Tensor getMinDimTensor(Vector<Tensor> tensors){
        if(tensors.size()==0)
            return new Tensor();

        Tensor result = tensors.firstElement();
        for (Tensor tensor: tensors){
            if(!isNullTensor(tensor)){
                if(tensor.getDimensionality()<result.getDimensionality())
                    result = tensor;
            }
        }
        return result;
    }


    /**sort tensors by their dimensionality
     * starting with max-dim Tensor
     * @param tensors tensors to be sorted
     */
    public static Tensor[] sortByDimentionlityMinFirst(Tensor ... tensors){
        Arrays.sort(tensors);
        return tensors;
    }

 /**Omit all dimensions of size 1 from Tensor tail
     * @param tensor tensor for reshape
     * @return equivalent Tensor with omitted one-sized dims
     */
    public static Tensor omitZeroSizedDimsFromTail(Tensor tensor, int minDims){
        if(isNullTensor(tensor))
            return null;

        Tensor result = new Tensor(tensor);

        while (result.getLastDimSize()==0 && result.getDimensionality()>minDims){
            result.removeDimension();
        }

        return result;
    }


    /**Omit all dimensions of size 1 from Tensor tail
     * @param tensor tensor for reshape
     * @return equivalent Tensor with omitted one-sized dims
     */
    public static Tensor omitOneSizedDimsFromTail(Tensor tensor, int minDims){
        if(isNullTensor(tensor))
            return null;
        if(tensor.getDimensionality()<2)
            return tensor;

        Tensor result = new Tensor(tensor);

        while (result.getLastDimSize()==1 && result.getDimensionality()>minDims){
            result.removeDimension();
        }

        return result;
    }

    /**Omit all dimensions of size 1 with specified ids
     * @param tensor tensor for reshape
     * @param dims dimensions to be checked
     * @return equivalent Tensor with omitted one-sized dims
     */
    public static Tensor omitOneSizedDims(Tensor tensor, int ... dims){
        if(isNullTensor(tensor))
            return null;
        Vector<Integer> skipList = new Vector<>();

        for(int dim: dims){
            if(tensor.getDimSize(dim)==1)
                skipList.add(dim);
        }

        Tensor result = new Tensor();
        for(int i=0; i<tensor.getDimensionality();i++){
            if(!skipList.contains(i))
                result.addDimension(tensor.getDimSize(i));
        }

        return result;
    }


    /**
     * Get last dimension size of the Tensor
     * @return last dimension size of the Tensor
     */
     public int getLastDimSize() { return _shape.lastElement(); }


     /**
     * Add pads to 2D subtensor.
     * [delta_height (top),delta_height(bottom), delta_width(left), delta_width *right]
     * and added to corresponding axis of Tensor
     * Malformed or empty pads are nor processed
     * to espam.Tensor in format [width, height, D3,...DN]
     * @param tensor Data Tensor in espam.Tensor format
     * @param pads pads to be added
     */
    public static Tensor addPads(Tensor tensor, int[] pads){
        if(pads==null)
            return tensor;
        if(Tensor.isNullOrEmpty(tensor))
            return tensor;
        if(pads.length!=4)
            return tensor;

        Tensor extended = new Tensor(tensor);
        int sumDeltaH = pads[0] + pads[1];
        int sumDeltaW = pads[2] + pads[3];
        extended.setDimSize(0,tensor.getDimSize(0) + sumDeltaW);
        extended.setDimSize(1,tensor.getDimSize(1) + sumDeltaH);
        return extended;
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private methods                       ////

    /**
     * Add empty dimensions to the tensor
     * @param tensor initial tensor
     * @param newDimsNum number of dimensions to be added
     * @return initial tensor, expanded with new dimensions
     */
    private static Tensor align(Tensor tensor,int newDimsNum) {
      Tensor result = new Tensor(tensor);

        for(int i=0;i<newDimsNum;i++)
            result.addDimension(1);

        return result;
  }

    /**
     * Automatically calculates and sets data chanels number
     * @param tensor tensor to be updated
     */
    public static void updateChannelsNum(Tensor tensor){
      if(Tensor.isNullOrEmpty(tensor))
          return;
    }

    /**
     * Set height of specified Tensor
     * @param tensor tensor to be updated
     * @param newHeight new height
     * @throws NullPointerException if the height could not be updated
     */
    public static void setHeight(Tensor tensor, int newHeight) throws NullPointerException{
        tensor.setDimSize(1, newHeight);
    }

  /**
     * Get height of specified Tensor
     * @param tensor tensor
     * @return tensor's height
     * @throws NullPointerException if the height could not be get
     */
     public static int getHeight(Tensor tensor) throws NullPointerException{
       return tensor.getDimSize(1);
  }

   /**
     * Checks, if neuron input DataFormat have height
     * @return true, if neuron input format have height and false otherwise
     */
    public static boolean isHaveHeight(Tensor tensor){
         if(Tensor.isNullOrEmpty(tensor))
            return false;

        if(tensor.getDimensionality()<2)
            return false;

        return true;
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////
    /**
     * Describes sizes of all dimensions inside of Tensor
     */
    @SerializedName("shape") private Vector<Integer> _shape;
}
