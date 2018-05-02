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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

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
    
    static ArrayList<SentencePair> pairs=new ArrayList<SentencePair>();
    static ArrayList<WordAsVec> vecs=new ArrayList<WordAsVec>();
    
    public static void main(String args[]) {
        File file1;
        File file2;
        if (args.length > 0) {
            file1 = new File(args[0]);
        } else {
            file1 = showGUI(sentence);
        }
        if (args.length > 1) {
            file2 = new File(args[0]);
        } else {
            file2 = showGUI(wordvec);
        }
        if (file1 == null || file2 == null) {
            System.out.println("No file selected");
        }
        try {
            BufferedReader sreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
            String line = sreader.readLine();
            while (line != null) {
                String sentence[] = line.split("\t");
                int type = -1;
                if (line.startsWith("contradiction\t")) {
                    type=0;
                } else if (line.startsWith("neutral\t")) {
                    type=1;
                } else if (line.startsWith("entailment\t")) {
                    type=2;
                }
                pairs.add(new SentencePair(sentence[3], sentence[4],type));

                line = sreader.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedReader vreader = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
            String line = vreader.readLine();
            WordAsVec.vecSize=Integer.parseInt(line.split(" ")[1]);
            line=vreader.readLine();
            while (line != null) {
                vecs.add(new WordAsVec(line));
                line=vreader.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(MultiNLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
