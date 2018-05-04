/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import smile.classification.LogisticRegression;

/**
 *
 * @author Lacko
 */
public class MultiNLI {

  static FileFilter sentence = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return file.getName().endsWith(".json") || file.getName().endsWith(".txt") || file.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Sentences";
    }
  };
  static FileFilter wordvec = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return file.getName().endsWith(".gz") || file.getName().endsWith(".txt") || file.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Word vectors";
    }
  };

  public static File showGUI(FileFilter filter) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(filter);
    chooser.showOpenDialog(null);
    chooser.getSelectedFile();
    return chooser.getSelectedFile();
  }

  public static void main(String args[]) {
    File file1;
    File file2;
    if (args.length > 0 && args[0].equals("-gui")) {
      file1 = showGUI(sentence);
      file2 = showGUI(wordvec);
    } else {
      if (args.length > 0) {
        file1 = new File(args[0]);
      } else {
        file1 = showGUI(sentence);
      }
      if (args.length > 1) {
        file2 = new File(args[1]);
      } else {
        file2 = showGUI(wordvec);
      }
    }
    if (file1 == null || file2 == null) {
      System.out.println("No file selected");
    }
    ArrayList<WordAsVec> vecs = new ArrayList<>();
    try (BufferedReader vreader = new BufferedReader(new InputStreamReader(new FileInputStream(file2)))) {
      String line = vreader.readLine();
      WordAsVec.vecSize = Integer.parseInt(line.split(" ")[1]);
      vecs = new ArrayList<WordAsVec>(Integer.parseInt(line.split(" ")[0]));
      line = vreader.readLine();
      while (line != null) {
        vecs.add(new WordAsVec(line));
        line = vreader.readLine();
      }
    } catch (IOException ex) {
      Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.err.format("%d vectors read in...", vecs.size());

    LinkedList<double[]> featuresAsList = new LinkedList<>();
    LinkedList<Integer> typesAsList = new LinkedList<>();
    try (BufferedReader sreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1)))) {
      sreader.readLine(); // the first line is just the header anyway, so skip it
      String line = sreader.readLine();
      int lineCounter = 1;
      while (line != null) {
        String sentence[] = line.split("\t");
        int type = -1;
        if (line.startsWith("contradiction\t")) {
          type = 0;
        } else if (line.startsWith("neutral\t")) {
          type = 1;
        } else if (line.startsWith("entailment\t")) {
          type = 2;
        }
        typesAsList.add(type);
        SentencePair sentencePair = new SentencePair(sentence[3], sentence[4], type);
        double[] featuresOfSentencePair = sentencePair.getSentencePairVec(vecs); // TODO fix this!!!
        featuresAsList.add(featuresOfSentencePair);
        line = sreader.readLine();
        if (++lineCounter % 150000 == 0) {
          break;
        }
        if (lineCounter % 10000 == 0) {
          System.err.println(lineCounter + " " + line);
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
    }
    double[][] features = new double[featuresAsList.size()][WordAsVec.vecSize * WordAsVec.vecSize];
    int i = 0;
    for (double[] f : featuresAsList) {
      features[i++] = f;
    }
    int[] types = new int[typesAsList.size()];
    i = 0;
    for (Integer type : typesAsList) {
      types[i++] = type;
    }
    LogisticRegression regression = new LogisticRegression(features, types); // TODO handle proper model selection (e.g. cross-validation)
    int prediction = regression.predict(features[0]);
    System.err.println(prediction + " " + types[0]);
  }
}
