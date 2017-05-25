package com.sj.sj.clipboardshare.ClipboardManager;

import android.os.Parcel;
import android.os.Parcelable;

public class ClipObject implements Parcelable {

    private String string;

    ClipObject(String string) {
        this.string = string;
    }

    private ClipObject(Parcel in) {
        string = in.readString();
    }

    public static final Creator<ClipObject> CREATOR = new Creator<ClipObject>() {
        @Override
        public ClipObject createFromParcel(Parcel in) {
            return new ClipObject(in);
        }

        @Override
        public ClipObject[] newArray(int size) {
            return new ClipObject[size];
        }
    };

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(string);
    }
}
