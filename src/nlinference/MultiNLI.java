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
import java.util.HashSet;
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
    private static boolean normalize = false;
    private static String genre = "#ALL";

    private static void processArgs(String args[], File files[][]) {
        boolean noTrainFound = false;
        int genreIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-notrain")) {
                noTrainFound = true;
            }
            if (args[i].equalsIgnoreCase("-norm")) {
                normalize = true;
            }
            if (args[i].equalsIgnoreCase("-genre")) {
                try {
                    genre = args[i + 1];
                    genreIndex = i + 1;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println("No genre given.");
                }
            }
        }
        files[0] = new File[noTrainFound ? 1 : 3];
        int at = 0;
        for (int i = 0; i < args.length; i++) {
            if (i != genreIndex && (!args[i].startsWith("-"))) {
                if (noTrainFound) {
                    files[0][0] = new File(args[i]);
                    break;
                } else {
                    files[0][at] = new File(args[i]);
                    at++;
                }
            }
        }
        if (!noTrainFound && at < 3) {
            File lastUsedFiles[] = rememberLastClosedLocations();

            String prefix0 = "Training data:" + (files[0][0] == null ? "missing" : files[0][0]);
            String prefix1 = "Training data:" + (files[0][1] == null ? "missing" : files[0][1]);
            String prefix2 = "Training data:" + (files[0][2] == null ? "missing" : files[0][2]);

            String postfix0 = lastUsedFiles[0] == null ? " Nothing can be used" : ". Using last known location:" + lastUsedFiles[0];
            String postfix1 = lastUsedFiles[1] == null ? " Nothing can be used" : ". Using last known location:" + lastUsedFiles[1];
            String postfix2 = lastUsedFiles[2] == null ? " Nothing can be used" : ". Using last known location:" + lastUsedFiles[2];

            for (int i = 0; i < 3; i++) {
                files[0][i]=lastUsedFiles[i];
            }

            System.out.println(prefix0 + postfix0);
            System.out.println(prefix1 + postfix1);
            System.out.println(prefix2 + postfix2);
        }
    }
    HashSet<String> genreSet = new HashSet<String>();

    void findGenres(File sentence_file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sentence_file));
            String line = reader.readLine();
            line = reader.readLine();
            while (line != null) {
                String elements[] = line.split("\t");
                genreSet.add(elements[9]);
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        File sentence_file = null;
        File wordvec_file = null;
        File trial_file = null;

        //File files[] = rememberLastClosedLocations();
        File file[][] = new File[1][];
        processArgs(args, file);

        if (file[0][0] != null) {
            sentence_file = file[0][0];
        }
        if (file[0][1] != null) {
            wordvec_file = file[0][1];
        }
        if (file[0][2] != null) {
            trial_file = file[0][2];
        }

        if (sentence_file != null && wordvec_file != null && trial_file != null) {
            memorizeLastSelectedLocation(sentence_file, wordvec_file, trial_file);
        }
        if (sentence_file == null || wordvec_file == null) {
            System.out.println("No file selected");
            System.exit(-1);
        }

        ArrayList<WordAsVec> vecs = new ArrayList<WordAsVec>();
        getWordVecs(vecs, wordvec_file);

        double features[][][] = new double[1][][];
        int types[][] = new int[1][];
        HashMap<String,double[]> vecMap=getData(vecs, features, types, sentence_file);

        float found = (float) SentencePair.foundWords / (float) SentencePair.wordCount;
        System.out.println(found);
       
        LogisticRegression regression = new LogisticRegression(features[0], types[0]); // TODO handle proper model selection (e.g. cross-validation)
        testData(regression,vecMap,sentence_file.getName(),trial_file,genre);
        
    }

    public static int[] testData(LogisticRegression regression, HashMap<String, double[]> vecs,String training, File file,String genre) {
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
                    double sentenceVec[] = pair.getSentencePairVec(vecs);
                    int predType = regression.predict(sentenceVec);
                    matrix[predType * 3 + type]++;
                }
                line = bin.readLine();
            }
            for(int i=0;i<3;i++){
                System.out.println(matrix[i*3]+" "+matrix[i*3+1]+" "+matrix[i*3+2]);
            }
            int overall=0;
            int allcorrect=0;
            for(int i=0;i<9;i++){
                overall+=matrix[i];
            }
            float correct[]=new float[3];
            for(int i=0;i<3;i++){
                allcorrect+=matrix[i*3+i];
                correct[i]=(float)matrix[i*3+i]/(float)(matrix[i*3]+matrix[i*3+1]+matrix[i*3+2]);
            }
            System.out.println("training data: "+training+" "+"genre: "+genre+(normalize?"normalized":""));
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("contradiction: "+correct[0]);
            System.out.println("neutral: "+correct[1]);
            System.out.println("entailment: "+correct[2]);
            System.out.println("");
            System.out.println("overall: "+((float)allcorrect/(float)overall));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matrix;
    }

    public static HashMap<String,double[]> getData(ArrayList<WordAsVec> vecs, double[][][] feature, int typearr[][], File file1) {
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
        return wordVecMap;
    }

    private static void memorizeLastSelectedLocation(File file1, File file2, File file3) {
        try (FileWriter writer = new FileWriter(new File("lastLoc"))) {
            writer.write(file1.getAbsolutePath() + "\n");
            writer.write(file2.getAbsolutePath() + "\n");
            writer.write(file3.getAbsolutePath() + "\n");
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
