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
public class SentencePair {
  private int type = -1;

  private String Atokens[];
  private String Btokens[];

  private SentenceTree ATree;
  private SentenceTree BTree;

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

  public SentencePair(String Asentence, String Bsentence, int type) {
    this.ATree = buildTree(Asentence);
    this.BTree = buildTree(Bsentence);

    Atokens = ATree.getSentence().split(" ");
    Btokens = BTree.getSentence().split(" ");

    this.type = type;
  }

  public SentencePair(String Asentence, String Bsentence) {
    this.ATree = buildTree(Asentence);
    this.BTree = buildTree(Bsentence);

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

  public double[] getSentencePairVec(ArrayList<WordAsVec> vecs) {
    WordAsVec Asentence[] = new WordAsVec[Atokens.length];
    System.out.println("sent");
    for (int i = 0; i < Atokens.length; i++) {
      Asentence[i] = WordAsVec.find(vecs, Atokens[i]);
    }
    System.out.println("sent");
    WordAsVec Bsentence[] = new WordAsVec[Btokens.length];
    for (int i = 0; i < Btokens.length; i++) {
      Bsentence[i] = WordAsVec.find(vecs, Btokens[i]);
    }
    System.out.println("sent");
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
