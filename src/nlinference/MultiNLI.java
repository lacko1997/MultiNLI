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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

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

    public static File showGUI(FileFilter filter, File file) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(file);
        chooser.showOpenDialog(null);
        chooser.getSelectedFile();
        return chooser.getSelectedFile();
    }

    public static void main(String args[]) {
        File sentence_file = null;
        File wordvec_file = null;
        File trial_file = null;

        File files[] = rememberLastClosedLocations();

        switch (args.length) {
            case 0:
                System.out.println("MultiNLI program help:");
                System.out.println("The first parameter is the path to the training data.");
                System.out.println("The second parameter is the path to the word vectors.");
                System.out.println("The third parameter is the path to the testing data.");
                System.out.println("-gui: It must be in the first parameter. If set it opens a file selector GUI.");
                System.out.println("-notrain: It must be in the first parameter. If set, you have to onlyí give the\ntesting data in the second parameter.");
                System.exit(0);
                break;
            case 1:
                if (args[0].equals("-notrain")) {
                    System.out.println("No testing file was given.");
                } else if (args[0].equals("-gui")) {
                    if (files[0] != null) {
                        sentence_file = showGUI(sentence, files[0]);
                    } else {
                        sentence_file = showGUI(sentence);
                    }
                    if (files[1] != null) {
                        wordvec_file = showGUI(wordvec, files[1]);
                    } else {
                        wordvec_file = showGUI(wordvec);
                    }
                    if (files[2] != null) {
                        wordvec_file = showGUI(wordvec, files[1]);
                    } else {
                        wordvec_file = showGUI(wordvec);
                    }
                } else {
                    System.out.println(args[0] + "is not a valid switch");
                }
            case 2:
                if (args[0].equals("-notrain")) {
                    trial_file = new File(args[1]);
                } else {
                    System.out.println("Switch needs only one parameter");
                }
                break;
            case 3:
                sentence_file=new File(args[0]);
                wordvec_file=new File(args[1]);
                trial_file=new File(args[2]);
                break;
        }

        if (sentence_file != null && wordvec_file != null) {
            memorizeLastSelectedLocation(sentence_file, wordvec_file);
        }
        if (sentence_file == null || wordvec_file == null) {
            System.out.println("No file selected");
            System.exit(-1);
        }

        ArrayList<WordAsVec> vecs = new ArrayList<WordAsVec>();
        getWordVecs(vecs, wordvec_file);

        double features[][][] = new double[1][][];
        int types[][] = new int[1][];
        getData(vecs, features, types, sentence_file);

        float found = (float) SentencePair.foundWords / (float) SentencePair.wordCount;
        System.out.println(found);
        for (int j = 0; j < 100; j++) {
            LogisticRegression regression = new LogisticRegression(features[0], types[0]); // TODO handle proper model selection (e.g. cross-validation)
            int prediction = regression.predict(features[0][j]);
            System.err.println(prediction + " " + types[0][j]);
        }
    }

    public static int[] testData(LogisticRegression regression, HashMap<String, double[]> vecs, File file) {
        int matrix[] = new int[9];
        try {
            BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = bin.readLine();
            while (line != null) {
                String sentences[] = line.split("\t");

                int type = -1;
                if (line.startsWith("contradiction\t")) {
                    type = 0;
                } else if (line.startsWith("neutral\t")) {
                    type = 1;
                } else if (line.startsWith("entailment\t")) {
                    type = 2;
                }
                if (type != -1) {
                    SentencePair pair = new SentencePair(sentences[3], sentences[4]);
                    double sentenceVec[]=pair.getSentencePairVec(vecs);
                    int predType=regression.predict(sentenceVec);
                    matrix[predType*3+type]++;
                }
                line = bin.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matrix;
    }

    public static void getData(ArrayList<WordAsVec> vecs, double[][][] feature, int typearr[][], File file1) {
        HashMap<String, double[]> wordVecMap = new HashMap<String, double[]>();
        for (int i = 0; i < vecs.size(); i++) {
            wordVecMap.put(vecs.get(i).getWord(), vecs.get(i).getWordvec());
        }

        System.err.format("%d vectors read in...", vecs.size());

        vecs.sort(WordAsVec.comp);

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
                if (type != (-1)) {
                    typesAsList.add(type);
                    SentencePair sentencePair = new SentencePair(sentence[3], sentence[4], type);
                    double[] featuresOfSentencePair = sentencePair.getSentencePairVec(wordVecMap); // TODO fix this!!!
                    featuresAsList.add(featuresOfSentencePair);
                }
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
        feature[0] = features;
        int i = 0;
        for (double[] f : featuresAsList) {
            features[i++] = f;
        }
        int[] types = new int[typesAsList.size()];
        typearr[0] = types;
        i = 0;
        for (Integer type : typesAsList) {
            types[i++] = type;
        }
    }

    private static void memorizeLastSelectedLocation(File file1, File file2) {
        try (FileWriter writer = new FileWriter(new File("lastLoc"))) {
            writer.write(file1.getAbsolutePath() + "\n");
            writer.write(file2.getAbsolutePath() + "\n");
            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Iaxception");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }

    private static void getWordVecs(ArrayList<WordAsVec> vecs, File file2) {
        if (!file2.getName().endsWith(".gz")) {
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
        } else {
            try (BufferedReader vreader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file2))))) {
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
        }

    }

    private static File[] rememberLastClosedLocations() {
        File files[] = new File[3];
        try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(new File("lastLoc"))))) {
            String str = fin.readLine();
            if (str != null) {
                files[0] = new File(str);
            } else {
                files[0] = null;
            }

            str = fin.readLine();
            if (str != null) {
                files[1] = new File(str);
            } else {
                files[1] = null;
            }

            str = fin.readLine();
            if (str != null) {
                files[2] = new File(str);
            } else {
                files[2] = null;
            }
        } catch (IOException ex) {

        }
        return files;
    }
}
