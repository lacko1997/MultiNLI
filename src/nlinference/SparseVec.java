package nlinference;

import java.util.ArrayList;

public class SparseVec {
    int nonZeroInds[];
    double values[];
    
    public SparseVec(String raw){
        String segments[]=raw.split(" ");
        ArrayList<Integer> inds=new ArrayList<Integer>(segments.length);
        ArrayList<Double> vals=new ArrayList<Double>(segments.length);
        for(int i=1;i<segments.length;i++){
            double value=Double.parseDouble(segments[i]);
            if(value!=0.0){
                inds.add(i);
                vals.add(value);
            }
        }
        nonZeroInds=new int[inds.size()];
        values=new double[inds.size()];
        for(int i=0;i<inds.size();i++){
            nonZeroInds[i]=inds.get(i);
            values[i]=vals.get(i);
        }
    }
}
