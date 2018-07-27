/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author Lacko
 */
class WordAsVec {

    public static int vecSize = 0;
    public static final String UNKNOWN = "#UNKNOWN";

    private String word;
    private double wordvec[];

    WordAsVec(String raw) {
        String segments[] = raw.split(" ");
        wordvec = new double[vecSize];
        for (int i = 1; i <= vecSize; i++) {
            wordvec[i - 1] = Double.parseDouble(segments[i]);
        }
        word = segments[0].toLowerCase();
    }

    WordAsVec() {
        wordvec = new double[vecSize];
        word = UNKNOWN;
    }

    WordAsVec(double wordvec[]) {
        this.word = UNKNOWN;
        this.wordvec=new double[wordvec.length];
        for (int i = 0; i < wordvec.length; i++) {
            this.wordvec[i] = wordvec[i];
        }
    }

    WordAsVec(String word, double wordvec[]) {
        this.word = word;
        this.wordvec=new double[wordvec.length];
        for (int i = 0; i < wordvec.length; i++) {
            this.wordvec[i] = wordvec[i];
        }
    }

    public void normalize() {
        double sqsum = 0.0f;
        for (double f : wordvec) {
            sqsum += (f * f);
        }
        double vecLength = Math.sqrt(sqsum);
        for (int i = 0; i < wordvec.length; i++) {
            wordvec[i] /= vecLength;
        }
    }

    boolean isZeroVec() {
        for (double f : wordvec) {
            if (f != 0.0) {
                return false;
            }
        }
        return true;
    }

    public void add(WordAsVec v2) {
        for (int i = 0; i < vecSize; i++) {
            this.wordvec[i] += v2.wordvec[i];
        }
    }

    static float dot(WordAsVec v1, WordAsVec v2) {
        float result = 0.0f;
        for (int i = 0; i < vecSize; i++) {
            result += v1.wordvec[i] * v2.wordvec[i];
        }
        return result;
    }

    static WordAsVec avarge(WordAsVec vecs[]) {
        WordAsVec vec = new WordAsVec();
        for (int i = 0; i < vecs.length; i++) {
            vec.add(vecs[0]);
        }
        vec.div(vecs.length);
        return vec;
    }

    public static WordAsVec diff(WordAsVec v1, WordAsVec v2) {
        double resultvec[] = new double[vecSize];
        for (int i = 0; i < vecSize; i++) {
            resultvec[i] = v1.wordvec[i] - v2.wordvec[i];
        }
        return new WordAsVec(resultvec);
    }

    static WordAsVec sum(WordAsVec v1, WordAsVec v2) {
        double resultvec[] = new double[vecSize];
        for (int i = 0; i < vecSize; i++) {
            resultvec[i] = v1.wordvec[i] + v2.wordvec[i];
        }
        return new WordAsVec(resultvec);
    }

    public double[] getWordvec() {
        return wordvec;
    }

    public String getWord() {
        return word;
    }

    public static WordAsVec find(HashMap<String, double[]> wordvecs, String word) {
        double valueVec[] = wordvecs.get(word);
        if (valueVec != null) {
            return new WordAsVec(word, valueVec);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object vec) {
        return ((vec instanceof String) && ((String) vec).equals(this.word)) || ((vec instanceof WordAsVec) && ((WordAsVec) vec).word.equals(this.word));
    }

    public static Comparator<WordAsVec> comp = (WordAsVec o1, WordAsVec o2) -> o1.word.compareTo(o2.word);

    public void mul(double multi) {
        for (int i = 0; i < vecSize; i++) {
            wordvec[i] *= multi;
        }
    }

    public void div(double divide) {
        for (int i = 0; i < vecSize; i++) {
            wordvec[i] /= divide;
        }
    }
}
