package espam.main.cnnUI;

public enum DNNInitRepresentation {
    LAYERBASED, NEURONBASED, BLOCKBASED;

    public static String toCommandLineParam(DNNInitRepresentation mode){
        switch (mode){
            case LAYERBASED: return "-lb";
            case NEURONBASED: return "-nb";
            case BLOCKBASED: return "-bb";
            default: return "";
        }


    }
}
