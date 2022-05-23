package application;

public class draft {
    public static void main(String[] args) {
        String s="order_table\tnull\tcontract_number\tproduct_model\tnull\tnull\tnull\tsalesman_number\tstop\tnull\tCSE0000101\tAttendanceMachineW1\tnull\tnull\tnull\t12110429\tstop";
        String[]s2=s.split("\t");
        for (int i = 0; i < s2.length; i++) {
            System.out.println(s2[i]);
        }
    }
}
