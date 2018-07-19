/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.util.ArrayList;

public class WordTypeResolver {
    class WeightedWord{
        String word;
        double weigth;

        public WeightedWord(String word,double weight) {
            this.word=word;
            this.weigth=weight;
        }
        
    }
    private void push(String type,int SP[],ArrayList<String> stack){
        if(SP[0]==stack.size()-1){
            stack.add(type);
            SP[0]++;
        }else{
            stack.set(SP[0], type);
            SP[0]++;
        }
    }
    private void pop(int SP[],ArrayList<String> stack){
        if(SP[0]>=0){
            stack.remove(SP[0]);
            SP[0]--;
        }
    }
    WordTypeResolver(String format_sent){
        int SP[]={-1};
        ArrayList<String> typeStack=new ArrayList<String>();
        String words[]=format_sent.split("\t");
        for(int i=0;i<words.length;i++){
            String word_types[]=words[i].split("/")[0].split(" ");
            String word=words[i].split("/")[1];
            for(int j=0;j<word_types.length;j++){
                
            }
        }
    }
}
