package myrelrec.myappl.jp.mytelrec;

import java.util.Comparator;

public class FileNameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        //年月日_時分秒の部分だけで比較
        String str1 = o1.substring( 0, 15 );
        String str2 = o2.substring( 0, 15 );

        //
        //正：str1はstr2より大きい
        //負：str1はstr2より小さい
        //0：str1とstr2は等しい
        //

        return str1.compareTo( str2 )*(-1);
    }
}
