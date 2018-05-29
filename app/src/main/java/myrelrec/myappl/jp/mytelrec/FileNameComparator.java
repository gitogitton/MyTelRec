package myrelrec.myappl.jp.mytelrec;

import java.util.Comparator;

public class FileNameComparator implements Comparator<ItemData> {
    @Override
    public int compare(ItemData o1, ItemData o2) {
        String str1 = o1.getPhoneNumber();
        String str2 = o2.getPhoneNumber();

        //
        //正：str1はstr2より大きい
        //負：str1はstr2より小さい
        //0：str1とstr2は等しい
        //

        return str1.compareTo( str2 ) * -1;
    }
}