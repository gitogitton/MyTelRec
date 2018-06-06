package myrelrec.myappl.jp.mytelrec;

import android.util.Log;

//
// ファイル拡張子文字列を定義
//
public enum EnumFormatList {

    WAV( "WAV" ),
    MP4( "MP4" );

    private final String type;

    /*private*/ EnumFormatList(String wav ) {
        this.type = wav;
        //Log.d( "test enum", "arg = "+wav );
    }

    public String getValue() {
        return this.type;
    }

}
