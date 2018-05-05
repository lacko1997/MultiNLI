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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import jdk.jfr.events.FileWriteEvent;
import org.netlib.util.doubleW;

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
        File file1;
        File file2;

        File files[] = rememberLastClosedLocations();
        if (args.length > 0 && args[0].equals("-gui")) {
            if (files[0] != null) {
                file1 = showGUI(sentence, files[0]);
            } else {
                file1 = showGUI(sentence);
            }
            if (files[1] != null) {
                file2 = showGUI(wordvec, files[1]);
            } else {
                file2 = showGUI(wordvec);
            }
        } else {
            if (args.length > 0) {
                file1 = new File(args[0]);
            } else {
                if (files[0] != null) {
                    file1 = showGUI(sentence, files[0]);
                } else {
                    file1 = showGUI(sentence);
                }
            }
            if (args.length > 1) {
                file2 = new File(args[1]);
            } else {
                if (files[1] != null) {
                    file2 = showGUI(wordvec, files[1]);
                } else {
                    file2 = showGUI(wordvec);
                }
            }
        }
        if(file1!=null&&file2!=null){
            memorizeLastSelectedLocation(file1, file2);
        }
        if (file1 == null || file2 == null) {
            System.out.println("No file selected");
            System.exit(-1);
        }
        ArrayList<WordAsVec> vecs = new ArrayList<WordAsVec>();
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
        
        HashMap<String,double[]> wordVecMap=new HashMap<String,double[]>();
        for(int i=0;i<vecs.size();i++){
            wordVecMap.put(vecs.get(i).getWord(),vecs.get(i).getWordvec());
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
                typesAsList.add(type);
                SentencePair sentencePair = new SentencePair(sentence[3], sentence[4], type);
                double[] featuresOfSentencePair = sentencePair.getSentencePairVec(wordVecMap); // TODO fix this!!!
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

        float found = (float) SentencePair.foundWords / (float) SentencePair.wordCount;
        System.out.println(found);

        /*LogisticRegression regression = new LogisticRegression(features, types); // TODO handle proper model selection (e.g. cross-validation)
        int prediction = regression.predict(features[0]);
        System.err.println(prediction + " " + types[0]);*/
    }

    private static void memorizeLastSelectedLocation(File file1, File file2) {
        try (FileWriter writer=new FileWriter(new File("lastLoc"))) {
            writer.write(file1.getAbsolutePath() + "\n");
            writer.write(file2.getAbsolutePath() + "\n");
            writer.close();
        } catch (FileNotFoundException ex) {
             System.out.println("Iaxception");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }

    private static File[] rememberLastClosedLocations() {
        File files[] = new File[2];
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
        } catch (IOException ex) {

        }
        return files;
    }
}
