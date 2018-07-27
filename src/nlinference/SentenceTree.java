/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.util.ArrayList;
import java.util.Comparator;

public class SentenceTree {
    SentenceTree parent=null;
    String type;
    double weight;
    String word;
    ArrayList<SentenceTree> children=new ArrayList<SentenceTree>();
    public SentenceTree(String type,String word){
        this.type=type;
        this.word=word;
    }
    
    public SentenceTree(){
        
    }
    
    public String getType(){
        return this.type;
    }
    public double getWeigth(){
        return weight;
    }
    public void setWeigth(double weight){
        this.weight=weight;
    }
    public String getWord(){
        return this.word;
    }
    
    public SentenceTree getParent(){
        return this.parent;
    }

    public void setParent(SentenceTree parent){
        this.parent=parent;
    }
    
    public void setType(String type){
        this.type=type;
    }
    
    public void addChild(SentenceTree child){
        children.add(child);
    }
    
    public ArrayList<SentenceTree> getChildren(){
        return this.children;
    }
    
    public String getSentence(){
        String str="";
        for(SentenceTree node:children){
            str+=node.getSentence();
        }
        if(this.word!=null){
            str+=word+" ";
        }
        return str;
    }
    
    private String getFormatSentence(){
        String str=" ("+this.getType()+" ";
        for(SentenceTree node:children){
            str+=node.getFormatSentence();
        }
        if(this.word!=null){
            str+=word+")";
        }else{
            str+=")";
        }
        return str;
    }
   
    @Override
    public String toString(){
        return getFormatSentence().replace("  "," ").substring(1);
    }
    public SentenceTree fildChildByType(String type){
        for(int i=0;i<children.size();i++){
            if(children.get(i).getType().equals(type)){
                return children.get(i);
            }
        }
        return null;
    }
}
