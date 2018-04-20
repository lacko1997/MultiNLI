package nlinference;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class StatViewer extends JPanel {

    ArrayList<Integer> neutstats = new ArrayList<Integer>();
    ArrayList<Integer> contstats = new ArrayList<Integer>();
    ArrayList<Integer> entastats = new ArrayList<Integer>();

    int SECTIONS=20;
    public StatViewer(ArrayList<SentencePair> npairs,ArrayList<SentencePair> cpairs,ArrayList<SentencePair> epairs) {
        for (int i = 0; i < SECTIONS; i++) {
            neutstats.add(0);
            contstats.add(0);
            entastats.add(0);
        }
        for (SentencePair pair : npairs) {
            float jaccard = pair.JaccardPerCharAvarge();
            for (int i = 0; i < SECTIONS; i++) {
                if (i * 1.0f/SECTIONS < jaccard && jaccard <= (i + 1) * 1.0f/SECTIONS) {
                    neutstats.set(i, neutstats.get(i) + 1);
                    break;
                }
            }
        }
        for (SentencePair pair : cpairs) {
            float jaccard = pair.JaccardPerCharAvarge();
            for (int i = 0; i < SECTIONS; i++) {
                if (i * 1.0f/SECTIONS < jaccard && jaccard <= (i + 1) * 1.0f/SECTIONS) {
                    contstats.set(i, contstats.get(i) + 1);
                    break;
                }
            }
        }
        for (SentencePair pair : epairs) {
            float jaccard = pair.JaccardPerCharAvarge();
            for (int i = 0; i < SECTIONS; i++) {
                if (i * 1.0f/SECTIONS < jaccard && jaccard <= (i + 1) * 1.0f/SECTIONS) {
                    entastats.set(i, entastats.get(i) + 1);
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawLine(20, 20, 20, 260);
        g.drawLine(10, 250, 310, 250);
        for (int i = 0; i < SECTIONS; i++) {
            g.setColor(new Color(100,100,255-i*255/SECTIONS));
            g.fillRect(20 + i * 300/SECTIONS, 250 - neutstats.get(i)/2, 300/SECTIONS, neutstats.get(i)/2);
            g.setColor(new Color(0,0,0));
            g.drawRect(20 + i * 300/SECTIONS, 250 - neutstats.get(i)/2, 300/SECTIONS, neutstats.get(i)/2);
        }
        g.drawLine(20, 300, 20, 540);
        g.drawLine(10, 530, 310, 530);
        for (int i = 0; i < SECTIONS; i++) {
            g.setColor(new Color(255-i*255/SECTIONS,0,0));
            g.fillRect(20 + i * 300/SECTIONS, 530 - contstats.get(i)/2, 300/SECTIONS, contstats.get(i)/2);
            g.setColor(new Color(0,0,0));
            g.drawRect(20 + i * 300/SECTIONS, 530 - contstats.get(i)/2, 300/SECTIONS, contstats.get(i)/2);
        }
        for (int i = 0; i < SECTIONS; i++) {
            g.setColor(new Color(0,255-i*255/SECTIONS,0));
            g.fillRect(350 + i * 300/SECTIONS, 250 - entastats.get(i)/2, 300/SECTIONS, entastats.get(i)/2);
            g.setColor(new Color(0,0,0));
            g.drawRect(350 + i * 300/SECTIONS, 250 - entastats.get(i)/2, 300/SECTIONS, entastats.get(i)/2);
        }
        g.drawLine(350, 20, 350, 260);
        g.drawLine(340, 250, 700, 250);
    }
}
