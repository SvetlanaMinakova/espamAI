package espam.datamodel.graph.csdf.datasctructures;

import espam.datamodel.EspamException;
import espam.visitor.CSDFGraphVisitor;

import java.util.HashMap;
import java.util.Vector;

/**
 * Pair of integer indexes
 */
public class IndexPair {
    /**
     * Constructor to create new index pair
     */
    public IndexPair(int first,int second) {
        _first = first;
        _second = second;
    }

     /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    public void accept(CSDFGraphVisitor x) { x.visitComponent(this); }

    /**
      * Compares IndexPair with another object
      * @param obj Object to compare this Neuron with
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

         IndexPair ip = (IndexPair) obj;
         return (this._first == ip._first) && (this._second==ip._second);
       }


    /**
     * Groups vector of IndexPairs by first index in pair
     * @return HashMap with indexPairs grouped by first index in pair
     */
    public static  HashMap<Integer,Vector<Integer>> groupByFirstIndex(Vector<IndexPair> indexPairs){
        HashMap<Integer,Vector<Integer>> map = new HashMap<>();
        for(IndexPair pair : indexPairs)
            addIndexPairToFirstIndexGroup(pair,map);

        return map;
    }

     /**
     * Groups vector of IndexPairs by second index in pair
     * @return HashMap with indexPairs grouped by second index in pair
     */
    public static  HashMap<Integer,Vector<Integer>> groupBySecondIndex(Vector<IndexPair> indexPairs){
        HashMap<Integer,Vector<Integer>> map = new HashMap<>();
        for(IndexPair pair : indexPairs)
            addIndexPairToSecondIndexGroup(pair,map);

        return map;
    }

    /**
     * Checks are 2 vectors of IndexPairs equal to each other
     * @param vec1 vector1
     * @param vec2 vector2
     * @return true, if vectors are equal anf false otherwise
     */
    public static boolean isVecEqual(Vector<IndexPair> vec1, Vector<IndexPair> vec2){

        if(vec1==null && vec2==null)
            return true;

        if(vec1==null || vec2==null)
            return false;

        if(vec1.size()!=vec2.size())
            return false;
        for(int i=0; i<vec1.size();i++){
            if(!vec1.elementAt(i).equals(vec2.elementAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Adds an IndexPair to HashMap, grouped by first index in pair
     * @param pair pair to add
     * @param pairList a HashMap, grouped by first index in pair
     */
    private static void addIndexPairToFirstIndexGroup(IndexPair pair, HashMap<Integer,Vector<Integer>> pairList){
        if(!pairList.containsKey(pair._first))
            pairList.put(pair._first,new Vector<>());

        pairList.get(pair._first).add(pair._second);
    }

      /**
     * Adds an IndexPair to HashMap, grouped by second index in pair
     * @param pair pair to add
     * @param pairList a HashMap, grouped by second index in pair
     */
    private static void addIndexPairToSecondIndexGroup(IndexPair pair, HashMap<Integer,Vector<Integer>> pairList){
        if(!pairList.containsKey(pair._second))
            pairList.put(pair._second,new Vector<>());

        pairList.get(pair._second).add(pair._first);
    }


    /**
     * printout a vector of indexpairs
     */
    public static void printIndexPairsVec(Vector<IndexPair> indexPairsVec){
        if(indexPairsVec==null)
            return;
        System.out.println(" ");
        for(IndexPair ip: indexPairsVec)
            System.out.print(ip.getSecond() + "*"+ ip.getFirst() + " ");
        System.out.println();
    }



    /**
     * Get first element of index pair
     * @return first element of index pair
     */
    public int getFirst() {
        return _first;
    }

    /**
     * Get second element of index pair
     * @return second element of index pair
     */
    public int getSecond() {
        return _second;
    }

    /**
     * Set first element of index pair
     * @param first first element of index pair
     */
    public void setFirst(int first) {
        this._first = first;
    }
    /**
     * Set second element of index pair
     * @param second second element of index pair
     */
    public void setSecond(int second) {
        this._second = second;
    }

    /**
     * first element of index pair
     */
    private int _first;

    /**
     * second element of index pair
     */
    private int _second;

    @Override
    public String toString() { return ("["+_first+","+_second+"]"); }
}
