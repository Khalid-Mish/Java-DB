package edu.uob;
public class Row {

    private String[] content;

    public Row(String[] content) {
        this.content = content;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < content.length; i++) {
            s += content[i];
            if (i != content.length - 1) {
                s += "\t|\t";
            }
        }
        return s;
    }
}