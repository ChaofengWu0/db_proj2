package application;

public class draft {
    public static void main(String[] args) {
        String s="store\tAsia\tnull\tnull\tnull\tnull\tnull\t12110429";
        String[]s2=s.split("\t");
        for (int i = 0; i < s2.length; i++) {
            System.out.println(s2[i]);
        }
    }
}
