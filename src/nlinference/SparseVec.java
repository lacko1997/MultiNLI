package nlinference;

import java.util.ArrayList;

public class SparseVec {
    private int nonZeroInds[];
    private double values[];
    
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
    public SparseVec(){
    
    }

    public int[] getNonZeroInds() {
        return nonZeroInds;
    }
    public double[] getValues(){
        return values;
    }
    public void add(SparseVec vec){
        ArrayList<Integer> list=new ArrayList<Integer>();
        ArrayList<Double> dlist=new ArrayList<Double>();
        for(int i=0;i<nonZeroInds.length;i++){
            list.add(nonZeroInds[0]);
        }
        for(int i=0;i<vec.getNonZeroInds().length;i++);
    }
}
