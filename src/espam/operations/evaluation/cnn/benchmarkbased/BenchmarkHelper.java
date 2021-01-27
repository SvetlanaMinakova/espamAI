package espam.operations.evaluation.cnn.benchmarkbased;
import espam.operations.evaluation.platformDescription.OpBenchmark;

import java.util.Vector;

/**
 * This class looks for benchmarks of DNN layers, obtained from platform
 * description, and is used by benchmark-based evaluators for platform-aware
 * DNN evaluation
 */

public class BenchmarkHelper {

    /**
     * Find operator benchmark record
     * @param opName operator name. e.g. CONV(5_5_25)
     * @return operator benchmark record, specified in the platform
     * description or null (if such record does not exist)
     */
    public static OpBenchmark findOpEvalRecord(String opName, Vector<OpBenchmark> opBenchmarks){
        String opBaseName = _getOpNameWithoutHyperPar(opName);
        Vector<OpBenchmark> opsWithPrefix = _findOpsWithPrefix(opBaseName, opBenchmarks);
        OpBenchmark match;
        switch (opsWithPrefix.size()){
            case 0: {
                match = null;
                break;
            }
            case 1: {
                match = opsWithPrefix.firstElement();
                break;
            }
            default:{
                match = _findBestMatch(opName, opsWithPrefix);
                break;
            }
        }
        //if(match!=null)
        //    System.out.println("Match for " + opName + " (base: "+ opBaseName + ") : "+ match.get_name());
        return match;
    }

    /**
     * Iterate though multiple benchmarks, suitable for given operator and try to find the most
     * suitable evaluation
     * @param opName operator name
     * @param opsBenchMarks list of benchmarks, suitable for this operator
     * @return the most suitable benchmark
     */
    private static OpBenchmark _findBestMatch(String opName, Vector<OpBenchmark> opsBenchMarks) {
        Vector<Integer> opBenchmarkScores = _scoreOpBenchmarksByMatch(opName, opsBenchMarks);
        Integer maxScoreId = 0;
        OpBenchmark bestMatch = opsBenchMarks.elementAt(maxScoreId);
        Integer maxScore = opBenchmarkScores.elementAt(maxScoreId);
        Integer curScore;
        for(Integer curBMId=0; curBMId<opsBenchMarks.size(); curBMId++){
            curScore = opBenchmarkScores.elementAt(curBMId);
            if (curScore > maxScore){
                maxScoreId = curBMId;
                bestMatch = opsBenchMarks.elementAt(maxScoreId);
                maxScore = opBenchmarkScores.elementAt(maxScoreId);
            }
        }

        return bestMatch;
    }

    /**
     * creates a list of operator scores, that evaluate how suitable is every benchmark
     * for an operator evaluation
     * @param opName operator name
     * @param opsBenchMarks list of operator-suitable benchmarks
     * @return a list of operator scores, that evaluate how suitable is every benchmark
     *for an operator evaluation
     */

    private static Vector<Integer> _scoreOpBenchmarksByMatch( String opName, Vector<OpBenchmark> opsBenchMarks){
        Vector<Integer> scores = new Vector<>();
        Integer score;
        for (OpBenchmark obm: opsBenchMarks){
            score = _scoreOpMatch(opName, obm.get_name());
            scores.add(score);
        }
        return scores;
    }

    /**
     * Evaluate how well operator name corresponds to the benchmark name
     * @param opName operator name
     * @param opBenchmarkName operator benchmark name
     * @return
     */
    private static Integer _scoreOpMatch(String opName, String opBenchmarkName){
        Integer score =0;
        Integer maxLenToCheck = Math.min(opName.length(), opBenchmarkName.length());
        Character opChar, opBmChar;
        for(Integer charId = 0; charId < maxLenToCheck; charId++){
            opChar = opName.charAt(charId);
            opBmChar = opBenchmarkName.charAt(charId);
            if (opChar.equals(opBmChar))
                score++;
            else score--;
        }
        return score;
    }


        /**
         * Find subset of benchmarks, such that name of
         * every benchmark in this subset starts with given prefix
         * (case-independent comparison)
         * @param prefix  prefix
         * @param allBenchmarks benchmarks to search in
         * @return subset of benchmarks, such that name of
         * every benchmark in this subset starts with given prefix
         */
    private static Vector<OpBenchmark> _findOpsWithPrefix (String prefix, Vector<OpBenchmark> allBenchmarks){
        Vector<OpBenchmark> subset = new Vector<>();
        for (OpBenchmark obm: allBenchmarks){
            if (obm.get_name().startsWith(prefix))
                subset.add(obm);
        }
        return subset;
    }

    /**
     * Get op name without hyperparameters
     * @param opname operator name with or without hyperparameters
     * @return operator name without hyperparameters
     */
    private static String _getOpNameWithoutHyperPar(String opname){
        if (!opname.contains("("))
            return opname;

        Integer bracketId = opname.indexOf('(');
        String opnameWithoutHyperpar = opname.substring(0, bracketId);

        if (opnameWithoutHyperpar.contains("_")) {
            bracketId = opname.indexOf("_");
            opnameWithoutHyperpar = opname.substring(0, bracketId);
        }
        return opnameWithoutHyperpar;
    }
}
