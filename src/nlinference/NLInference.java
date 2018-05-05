/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlinference;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import smile.classification.LogisticRegression;
/**
 *
 * @author Lacko
 */
public class NLInference {

    static String lastUsedDirectory = null;
    static String lastWordVecDir = null;
    
    static ArrayList<SentencePair> Contradiction = new ArrayList<SentencePair>();
    static ArrayList<SentencePair> Neutral = new ArrayList<SentencePair>();
    static ArrayList<SentencePair> Entailment = new ArrayList<SentencePair>();
    
    static ArrayList<WordAsVec> WordVec=new ArrayList<WordAsVec>();

    /*public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JFileChooser chooser = new JFileChooser();
        File local = new File("cachedData");
        try (BufferedReader fin = new BufferedReader(new FileReader(local))) {
            String line = fin.readLine();
            int at = 0;
            while (line != null) {
                if (line.startsWith("file ")) {
                    switch (at) {
                        case 0:
                            lastUsedDirectory = line.split(" ")[1];
                            break;
                        case 1:
                            lastWordVecDir = line.split(" ")[1];
                            break;

                    }
                    //System.out.println(lastWordVecDir);
                    at++;
                    //break;
                }
                line = fin.readLine();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Not found");
        } catch (IOException ex) {
            Logger.getLogger(NLInference.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (lastUsedDirectory != null) {
            chooser.setCurrentDirectory(new File(lastUsedDirectory));
        }
        chooser.showOpenDialog(frame);
        File file = chooser.getSelectedFile();

        if (lastWordVecDir != null) {
            chooser.setCurrentDirectory(new File(lastWordVecDir));
        }
        
        chooser.showOpenDialog(frame);
        File words = chooser.getSelectedFile();
        if (file != null&&words!=null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter("cachedData"))) {
                writer.write("file " + file.getPath() + "\n");
                writer.write("file " + words.getPath() + "\n");
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(NLInference.class.getName()).log(Level.SEVERE, null, ex);
            }

            frame.setSize(800, 600);
            frame.setVisible(true);

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                while (line != null) {
                    try {
                        String sentences[] = line.split("\t");
                        if (line.startsWith("contradiction\t")) {
                            Contradiction.add(new SentencePair(sentences[3], sentences[4]));
                        } else if (line.startsWith("neutral\t")) {
                            Neutral.add(new SentencePair(sentences[3], sentences[4]));
                        } else if (line.startsWith("entailment\t")) {
                            Entailment.add(new SentencePair(sentences[3], sentences[4]));
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not the correct format");
                    }
                    line = reader.readLine();
                }
                if (words.getName().endsWith(".gz")) {
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(words))));
                }else{
                    reader = new BufferedReader(new FileReader(words));
                }
                line = reader.readLine();
                WordAsVec.vecSize=Integer.parseInt(line.split(" ")[1]);
                //System.out.println(WordAsVec.vecSize);
                line=reader.readLine();
                while (line != null) {
                    WordVec.add(new WordAsVec(line));
                    line = reader.readLine();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(NLInference.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NLInference.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("wordvecs");
            WordVec.sort(WordAsVec.comp);
            System.out.println("sorted wordvecs");
            /*for(WordAsVec w:WordVec){
                System.out.println(w.getWord());
            }
        }

        /*Contradiction.sort(SentencePair.compPerWord);
        Entailment.sort(SentencePair.compPerWord);
        Neutral.sort(SentencePair.compPerWord);*/
        
        /*double contr[][]=new double[WordAsVec.vecSize*WordAsVec.vecSize][Contradiction.size()];
        double ental[][]=new double[WordAsVec.vecSize*WordAsVec.vecSize][Entailment.size()];
        double neutr[][]=new double[WordAsVec.vecSize*WordAsVec.vecSize][Neutral.size()];
        double sentenceVec[][]=new double[WordAsVec.vecSize*WordAsVec.vecSize][Contradiction.size()+Entailment.size()+Neutral.size()];
        int groups[]=new int[Contradiction.size()+Entailment.size()+Neutral.size()];
        
        System.out.println("sentence pairing");
        for(int i=0;i<Contradiction.size();i++){
            sentenceVec[i]=Contradiction.get(i).getSentencePairVec(WordVec);
        }
        for(int i=0;i<Entailment.size();i++){
            sentenceVec[Contradiction.size()+i]=Entailment.get(i).getSentencePairVec(WordVec);
        }
        for(int i=0;i<Neutral.size();i++){
            sentenceVec[Contradiction.size()+Entailment.size()+i]=Neutral.get(i).getSentencePairVec(WordVec);
        }
        
        /*StatViewer panel = new StatViewer(Neutral, Contradiction, Entailment);
        frame.add(panel);
        
        System.out.println("start learning");
        LogisticRegression regression=new LogisticRegression(sentenceVec, groups);
        System.out.println(regression.loglikelihood());
        float sum = 0.0f;
        for (SentencePair pair : Entailment) {
            sum += pair.JaccardPerCharAvarge();
            //System.out.println(pair.JaccardPerCharAvarge());
        }
        System.out.println("Entailment AVG: " + (sum / Entailment.size()));

        sum = 0.0f;
        for (SentencePair pair : Neutral) {
            sum += pair.JaccardPerCharAvarge();
            //System.out.println(pair.JaccardPerCharAvarge());
        }
        System.out.println("Neutral AVG: " + (sum / Neutral.size()));

        sum = 0.0f;
        for (SentencePair pair : Contradiction) {
            sum += pair.JaccardPerCharAvarge();
            //System.out.println(pair.JaccardPerCharAvarge());
        }
        System.out.println("Contradiction AVG: " + (sum / Contradiction.size()));
    }*/

    /*static float calculateMedian(ArrayList<SentencePair> pairs) {
        if (pairs.size() % 2 == 0) {
            return (pairs.get(pairs.size() / 2).JaccardPerWord() + pairs.get(pairs.size() / 2 - 1).JaccardPerWord()) / 2.0f;
        } else {
            return pairs.get(pairs.size() / 2).JaccardPerWord();
        }
    }*/
}
