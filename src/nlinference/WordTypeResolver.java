/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static nlinference.SentencePair.wordCount;

public class WordTypeResolver {

    class WeightedWord {

        String word;
        double weigth;

        public WeightedWord(String word, double weight) {
            this.word = word;
            this.weigth = weight;
        }
    }

    class TypedWord {

        String word;
        String types[];

        public TypedWord(String word, ArrayList<String> typeStack, int endIndex) {
            this.word = word;
            this.types = new String[endIndex + 1];
            for (int i = 0; i < endIndex + 1; i++) {
                types[i] = typeStack.get(i);
            }
        }

        @Override
        public String toString() {
            String str = types[0];
            for (int i = 1; i < types.length; i++) {
                str += " " + types[i];
            }
            str += "/" + word;
            return str;
        }
    }

    private void push(String type, int SP[], ArrayList<String> stack) {
        if (SP[0] == stack.size() - 1) {
            stack.add(type);
            SP[0]++;
        } else {
            SP[0]++;
            stack.set(SP[0], type);
        }
    }

    private void pop(int SP[], ArrayList<String> stack) {
        if (SP[0] >= 0) {
            SP[0]--;
        }
    }

    /*double getWordWeightFromTypes(String types[]){
        
     }*/
    TypedWord twords[];

    void attachTypes(String format_sent) {
        int SP[] = {-1};
        ArrayList<String> typeStack = new ArrayList<String>();
        String words[] = format_sent.split("\t");
        twords = new TypedWord[words.length];
        for (int i = 0; i < words.length; i++) {
            int backCount = 1;
            while (words[i].startsWith("<")) {
                words[i] = words[i].substring(1);
                backCount++;
            }
            if (words[i].equals("")) {
                continue;
            }
            String parts[] = words[i].split("/");
            String word_types[] = parts[0].split(" ");
            String word = "";

            if (parts.length == 1) {
                word += "/";
            } else {
                word = parts[1];
            }

            for (int j = 0; j < word_types.length; j++) {
                push(word_types[j], SP, typeStack);
            }
            twords[i] = new TypedWord(word, typeStack, SP[0]);
            for (int j = 0; j < backCount; j++) {
                pop(SP, typeStack);
            }
        }
    }
    WeightedWord wordWeights[];

    private void calculateWordWeights() {
        double overallWeight =0.0f;
        int count=0;
        wordWeights = new WeightedWord[twords.length];
        for (int i = 0; i < this.twords.length; i++) {
            //System.out.println(twords[i]);
            if (twords[i] != null && twords[i].types != null) {
                for (String type : twords[i].types) {
                    if (SentencePair.typeWeights.get(type) != null) {
                        overallWeight += SentencePair.typeWeights.get(type);
                    }
                }
                
            } else {
                continue;
            }
            wordWeights[i] = new WeightedWord(twords[i].word, overallWeight/twords.length);
            //System.out.println(overallWeight/twords.length);
        }
        
    }

    public WordAsVec getWieghtedVecSum(HashMap<String, double[]> wordvecs) {
        WordAsVec sum = new WordAsVec();
        int count = 0;
        for (int i = 0; i < wordWeights.length; i++) {
            if (wordWeights[i] != null) { 
                wordCount++;
                if (wordvecs.get(wordWeights[i].word) != null) {
                    WordAsVec vec = new WordAsVec(wordWeights[i].word, wordvecs.get(wordWeights[i].word));
                    //System.out.println(Arrays.toString(wordvecs.get(wordWeights[i].word)));
                    vec.mul(wordWeights[i].weigth);
                    sum.add(vec);
                    count++;
                    SentencePair.foundWords++;
                }
            }
        }
        //System.out.println(count);
        if(count==0){
            count++;
        }
        sum.div(count);
        return sum;
    }

    WordTypeResolver(String format_sent) {
        attachTypes(format_sent);
        calculateWordWeights();
    }

    @Override
    public String toString() {
        String str = twords[0].toString();
        for (int i = 0; i < twords.length; i++) {
            str += "\n" + twords[i];
        }
        str += "\n";
        return str;
    }
}
