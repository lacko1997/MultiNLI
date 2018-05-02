/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author Lacko
 */
class WordAsVec {
    public static int vecSize=0;
    public static final String UNKNOWN="#UNKNOWN";
    
    private String word;
    private float wordvec[];
    WordAsVec(String raw){
        String segments[]=raw.split(" ");
        wordvec=new float[vecSize];
        for(int i=1;i<=vecSize;i++){
            wordvec[i-1]=Float.parseFloat(segments[i]);
        }
        word=segments[0];
    }
    WordAsVec(){
        wordvec=new float[vecSize];
        word=UNKNOWN;
    }
    WordAsVec(float wordvec[]){
        this.word=UNKNOWN;
        this.wordvec=wordvec;
    }
    private void add(WordAsVec v2){
        for(int i=0;i<vecSize;i++){
            this.wordvec[i]+=v2.wordvec[i];
        }
    }
    static float dot(WordAsVec v1,WordAsVec v2){
        float result=0.0f;
        for(int i=0;i<vecSize;i++){
            result+=v1.wordvec[i]*v2.wordvec[i];
        }
        return result;
    }
    static WordAsVec avarge(WordAsVec vecs[]){
        WordAsVec vec=new WordAsVec();
        for(int i=0;i<vecs.length;i++){
            vec.add(vecs[0]);
        }
        vec.div(vecs.length);
        return vec;
    }
    static WordAsVec diff(WordAsVec v1,WordAsVec v2){
        float resultvec[]=new float[vecSize];
        for(int i=0;i<vecSize;i++){
            resultvec[vecSize]=v1.wordvec[i]-v2.wordvec[i];
        }
        return new WordAsVec(resultvec);
    }
    static WordAsVec sum(WordAsVec v1,WordAsVec v2){
        float resultvec[]=new float[vecSize];
        for(int i=0;i<vecSize;i++){
            resultvec[vecSize]=v1.wordvec[i]+v2.wordvec[i];
        }
        return new WordAsVec(resultvec);
    }
    public float[] getWordvec(){
        return wordvec;
    }
    public String getWord(){
        return word;
    }
    public static WordAsVec find(ArrayList<WordAsVec> wordvecs,String word){
        int M=wordvecs.size()/2;
        int L=wordvecs.size()-1;
        int S=0;
        
        while(L>S){
            if(wordvecs.get(M).getWord().compareTo(word)>0){
                S=M+1;
                M=(L+S)/2;
            }else if(wordvecs.get(M).getWord().compareTo(word)<0){
                L=M;
                M=(L+S)/2;
            }else if(wordvecs.get(M).getWord().compareTo(word)==0){
                return wordvecs.get(M);
            }
            System.out.println(L+" "+S);
        }
        return null;
    }
    
    @Override
    public boolean equals(Object vec){
        return ((vec instanceof String)&&((String) vec).equals(this.word))||((vec instanceof WordAsVec)&&((WordAsVec) vec).word.equals(this.word));
    }
    
    public static Comparator<WordAsVec> comp=(WordAsVec o1, WordAsVec o2) -> o1.word.compareTo(o2.word);

    private void div(int length) {
        for(int i=0;i<vecSize;i++){
            wordvec[i]/=length;
        }
    }
}