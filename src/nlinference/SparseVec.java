package nlinference;

import java.util.ArrayList;
import java.util.Comparator;

public class SparseVec {

    private DataPair data[];

    private class DataPair {

        private int index;
        private double value;

        DataPair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        int getIndex() {
            return index;
        }

        double getValue() {
            return value;
        }

        public void flipSign() {
            this.value = -this.value;
        }

        public void divide(double divisor) {
            this.value /= divisor;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Comparator<DataPair> indexOrder = new Comparator<DataPair>() {

            @Override
            public int compare(DataPair t, DataPair t1) {
                if (t.index < t1.index) {
                    return -1;
                } else if (t.index == t1.index) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
    }

    public SparseVec(String raw) {
        String segments[] = raw.split(" ");
        ArrayList<DataPair> data = new ArrayList<DataPair>(segments.length);
        for (int i = 1; i < segments.length; i++) {
            double value = Double.parseDouble(segments[i]);
            if (value != 0.0) {
                data.add(new DataPair(i, value));
            }
        }

        for (int i = 0; i < data.size(); i++) {
            this.data[i] = data.get(i);
        }
    }

    public SparseVec() {
        data=new DataPair[0];
    }

    public DataPair[] getData() {
        return data;
    }

    public int[] getNonZeroInds() {
        int inds[] = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            inds[i] = data[i].getIndex();
        }
        return inds;
    }

    public double[] getValues() {
        double values[] = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = data[i].getValue();
        }
        return values;
    }

    public void add(SparseVec vec) {
        ArrayList<DataPair> pair = new ArrayList<DataPair>();
        DataPair pairs[] = vec.getData();
        int at = 0;
        for (int i = 0; i < pairs.length; i++) {
            if (at < this.data.length) {
                if (pairs[i].getIndex() < this.data[at].getIndex()) {
                    pair.add(pairs[i]);
                } else if (pairs[i].getIndex() == data[at].getIndex()) {
                    pair.add(new DataPair(pairs[i].getIndex(), pairs[i].getValue() + data[i].getValue()));
                    at++;
                } else {
                    pair.add(data[at]);
                    pair.add(pairs[i]);
                    at++;
                }
            } else {
                pair.add(pairs[i]);
            }
        }
        for (; at < data.length; at++) {
            pair.add(data[at]);
        }
        this.data = new DataPair[pair.size()];
        for (int i = 0; i < pair.size(); i++) {
            data[i] = pair.get(i);
        }
    }

    public void sub(SparseVec vec) {
        ArrayList<DataPair> pair = new ArrayList<DataPair>();
        DataPair pairs[] = vec.getData();
        int at = 0;
        for (int i = 0; i < pairs.length; i++) {
            pairs[i].flipSign();
            if (at < this.data.length) {
                if (pairs[i].getIndex() < this.data[at].getIndex()) {
                    pair.add(pairs[i]);
                } else if (pairs[i].getIndex() == data[at].getIndex()) {
                    pair.add(new DataPair(pairs[i].getIndex(), pairs[i].getValue() + data[i].getValue()));
                    at++;
                } else {
                    pair.add(data[at]);
                    pair.add(pairs[i]);
                    at++;
                }
            } else {
                pair.add(pairs[i]);
            }
        }
        for (; at < data.length; at++) {
            pair.add(data[at]);
        }
        this.data = new DataPair[pair.size()];
        for (int i = 0; i < pair.size(); i++) {
            data[i] = pair.get(i);
        }
    }

    public void div(double divisor) {
        for (int i = 0; i < data.length; i++) {
            data[i].divide(divisor);
        }
    }

    public void treshold(double tresh) {
        ArrayList<DataPair> pair = new ArrayList<DataPair>();
        for (int i = 0; i < data.length; i++) {
            if (data[i].getValue()>=tresh) {
                pair.add(data[i]);
            }
        }
        data=new DataPair[pair.size()];
        for (int i = 0; i < pair.size(); i++) {
            data[i]=pair.get(i);
        }
    }
}
