/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lacko
 */
public class SentencePair {

    private int type = -1;

    private String Atokens[];
    private String Btokens[];

    private SentenceTree ATree;
    private SentenceTree BTree;

    public static HashMap<String, Double> typeWeights = new HashMap<String, Double>();
    public static HashMap<String, Integer> typeCount = new HashMap<String, Integer>();
    public static HashSet<String> types = new HashSet<String>();

    public static void readTypes(File types) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(types)));
            String line = reader.readLine();
            while (line != null) {
                String type = line.split(" ")[0];
                double weight;
                try {
                    weight = Double.parseDouble(line.split(" ")[1]);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    weight = 1.0;
                }
                typeWeights.put(type, weight);
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SentencePair.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SentencePair.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private SentenceTree buildTree(String sentence) {
        SentenceTree parent = null;
        SentenceTree current = null;

        String segments[] = sentence.split(" ");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].startsWith("(")) {
                if (i + 1 < segments.length && segments[i + 1].endsWith(")")) {
                    String word = segments[i + 1];
                    while (word.endsWith(")")) {
                        word = word.substring(0, word.length() - 1);
                    }
                    // System.out.println(segments[i+1]);
                    current = new SentenceTree(segments[i].substring(1), word);
                } else {
                    current = new SentenceTree(segments[i].substring(1), null);
                }
                if (parent != null) {
                    current.setParent(parent);
                    parent.addChild(current);
                }
                parent = current;
            }
            while (segments[i].endsWith(")")) {
                current = parent;
                parent = current.getParent();
                segments[i] = segments[i].substring(0, segments[i].length() - 1);
            }
        }
        return current;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private String getMarkedSentencePair(SentenceTree tree) {
        String str = "";
        for (int i = 0; i < tree.children.size(); i++) {
            str += tree.children.get(i).type;
            if (tree.children.get(i).word == null) {
                str += " ";
                str += getMarkedSentencePair(tree.children.get(i));
            } else {
                str += "/";
                str += tree.children.get(i).word;
                str += "\t";
            }
        }
        str += "<";
        return str;
    }
    WordTypeResolver resolveA, resolveB;

    public SentencePair(String Asentence, String Bsentence, int type) {
        this.ATree = buildTree(Asentence);
        this.BTree = buildTree(Bsentence);

        Atokens = ATree.getSentence().split(" ");
        Btokens = BTree.getSentence().split(" ");

        resolveA = new WordTypeResolver(getMarkedSentencePair(ATree));
        resolveB = new WordTypeResolver(getMarkedSentencePair(BTree));
        //System.out.println(resolve);

        this.type = type;
    }

    public SentencePair(String Asentence, String Bsentence) {
        this.ATree = buildTree(Asentence);
        this.BTree = buildTree(Bsentence);
        
        resolveA = new WordTypeResolver(getMarkedSentencePair(ATree));
        resolveB = new WordTypeResolver(getMarkedSentencePair(BTree));

        Atokens = ATree.getSentence().split(" ");
        Btokens = BTree.getSentence().split(" ");
    }

    float JaccardPerCharAvarge() {
        float sum = 0.0f;
        int len = Atokens.length < Btokens.length ? Atokens.length : Btokens.length;
        for (int i = 0; i < len; i++) {
            int equal = 0;
            int wlen = Atokens[i].length() < Btokens[i].length() ? Atokens[i].length() : Btokens[i].length();
            for (int j = 0; j < wlen; j++) {
                if (Atokens[i].charAt(j) == Btokens[i].charAt(j)) {
                    equal++;
                }
            }
            sum += ((float) equal / (float) (Atokens[i].length() + Btokens[i].length() - equal));
        }
        int div = Atokens.length > Btokens.length ? Atokens.length : Btokens.length;
        return sum / div;
    }
    public static int wordCount = 0;
    public static int foundWords = 0;

    public double[] getWeightedSentencePairVec(HashMap<String, double[]> vecs) {
        if (vecs == null || resolveA == null || resolveB == null) {
            System.out.println(vecs + " " + resolveA + " " + resolveB);
        }
        WordAsVec A = resolveA.getWieghtedVecSum(vecs);
        WordAsVec B = resolveB.getWieghtedVecSum(vecs);
        return WordAsVec.diff(A, B).getWordvec();
    }

    private void getRecursiveTree(SentenceTree tree) {
        SentencePair.types.add(tree.getType());
        if (SentencePair.typeCount.containsKey(tree.getType())) {
            typeCount.replace(tree.getType(), typeCount.get(tree.getType()) + 1);
        } else {
            typeCount.put(tree.getType(), 1);
        }
        for (int i = 0; i < tree.getChildren().size(); i++) {
            getRecursiveTree(tree.getChildren().get(i));
        }
    }
    /*public void resolveSentencePair(){
     SentenceTree tree=ATree.findChildByType("NP");
     }*/

    public void getPhraseTypes() {
        for (int i = 0; i < ATree.getChildren().size(); i++) {
            getRecursiveTree(ATree.getChildren().get(i));
        }
        for (int i = 0; i < BTree.getChildren().size(); i++) {
            getRecursiveTree(BTree.getChildren().get(i));
        }
    }

    public double[] getSentencePairVec(HashMap<String, double[]> vecs) {
        //System.out.println("sent");
        int wordInSentenceA = 0;
        for (int i = 0; i < Atokens.length; i++) {
            WordAsVec vec = WordAsVec.find(vecs, Atokens[i]);
            wordCount++;
            if (vec != null && !vec.isZeroVec()) {
                wordInSentenceA++;
                foundWords++;
            }
        }
        int wordInSentenceB = 0;
        //System.out.println("sent");

        for (int i = 0; i < Btokens.length; i++) {
            WordAsVec vec = WordAsVec.find(vecs, Btokens[i]);
            wordCount++;
            if (vec != null && !vec.isZeroVec()) {
                wordInSentenceB++;
                foundWords++;
            }
        }
        WordAsVec Asentence[];
        WordAsVec Bsentence[];
        if (wordInSentenceA != 0) {
            Asentence = new WordAsVec[wordInSentenceA];
        } else {
            Asentence = new WordAsVec[1];
            Asentence[0] = new WordAsVec();
        }

        if (wordInSentenceB != 0) {
            Bsentence = new WordAsVec[wordInSentenceB];
        } else {
            Bsentence = new WordAsVec[1];
            Bsentence[0] = new WordAsVec();
        }

        int at = 0;
        for (int i = 0; i < Atokens.length; i++) {
            WordAsVec vec = WordAsVec.find(vecs, Atokens[i]);
            if (vec != null && !vec.isZeroVec()) {
                Asentence[at] = vec;
                at++;
            }
        }
        at = 0;
        for (int i = 0; i < Btokens.length; i++) {
            WordAsVec vec = WordAsVec.find(vecs, Btokens[i]);
            if (vec != null && !vec.isZeroVec()) {
                Bsentence[at] = vec;
                at++;
            }
        }

        WordAsVec Avec = WordAsVec.avarge(Asentence);
        WordAsVec Bvec = WordAsVec.avarge(Bsentence);
        double result[] = new double[WordAsVec.vecSize * WordAsVec.vecSize];
        for (int i = 0; i < WordAsVec.vecSize; i++) {
            for (int j = 0; j < WordAsVec.vecSize; j++) {
                result[i * WordAsVec.vecSize + j] = Avec.getWordvec()[i] * Bvec.getWordvec()[j];
            }
        }
        return result;
    }

    public int[] sparseSentencePairVec(HashMap<String, SparseVec> vecs) {
        SparseVec linesA = new SparseVec();
        SparseVec linesB = new SparseVec();

        int linesA_c = 0;
        int linesB_c = 0;

        for (int i = 0; i < Atokens.length; i++) {
            SparseVec curr = vecs.get(Atokens[i]);
            if (curr != null) {
                linesA.add(curr);
                linesA_c++;
            }
        }
        linesA.div(Atokens.length);
        linesA.treshold(0.75);

        for (int i = 0; i < Btokens.length; i++) {
            SparseVec curr = vecs.get(Btokens[i]);
            if (curr != null) {
                linesB.add(curr);
                linesB_c++;
            }
        }
        linesB.div(Btokens.length);
        linesB.treshold(0.25);

        ArrayList<Integer> result = new ArrayList<Integer>();
        int Avec[] = linesA.getNonZeroInds();
        int Bvec[] = linesB.getNonZeroInds();

        for (int i = 0; i < Avec.length; i++) {
            for (int j = 0; j < Bvec.length; i++) {
                result.add(Avec[i] * 100 + Bvec[i]);
            }
        }
        int res[] = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            res[i] = result.get(i);
        }
        return res;
    }

    public float JaccardPerWord() {
        int wordCount = Atokens.length < Btokens.length ? Btokens.length : Atokens.length;
        int lastWordIndex = Atokens.length > Btokens.length ? Btokens.length : Atokens.length;
        int equalCount = 0;
        for (int i = 0; i < lastWordIndex; i++) {
            if (Atokens[i].equals(Btokens[i])) {
                equalCount++;
            }
        }
        return (float) equalCount / ((float) (lastWordIndex + wordCount - equalCount));
    }

    public static Comparator<SentencePair> compPerWord = new Comparator<SentencePair>() {
        @Override
        public int compare(SentencePair t, SentencePair t1) {
            if (t.JaccardPerWord() < t1.JaccardPerWord()) {
                return 1;
            } else if (t.JaccardPerWord() > t1.JaccardPerWord()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

}
