package myrelrec.myappl.jp.mytelrec;

public class ItemData {
    private String mDate;
    private String mDisplayString; //mPhoneNumber, mName のいずれか
    private String mPhoneNumber;
    private String mName;

    public String getDate() {
        return this.mDate;
    }
    public void setDate( String date ) {
        this.mDate = date;
    }

    public String getPhoneNumber() {
        return this.mPhoneNumber;
    }
    public void setPhoneNumber( String phoneNumber ) {
        this.mPhoneNumber = phoneNumber;
    }

    public String getName() {
        return this.mName;
    }
    public void setName( String name ) {
        this.mName = name;
    }

    public String getDisplayString() {
        return mDisplayString;
    }

    public void setDisplayString( String displayString ) {
        this.mDisplayString = displayString;
    }
}
