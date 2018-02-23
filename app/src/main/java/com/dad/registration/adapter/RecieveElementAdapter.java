package com.dad.registration.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dad.R;
import com.dad.util.BitMapHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class RecieveElementAdapter extends BaseAdapter {

    public interface OnDeleteItemClickListner {
        void onDeleteItemClick(int position);
    }

    private int listLength;
    private Context context;
    private ArrayList<String> nickNameList;
    private ArrayList<String> fullNameList;
    private ArrayList<String> numberList;
    private ArrayList<String> emailList;
    private OnDeleteItemClickListner contactFragment;
    private final String TAG_FIRST_NAME = "firstname";
    private final String TAG_LAST_NAME = "lastname";
    private final String TAG_NICKNAME = "nickname";
    private final String TAG_EMAIL = "email";
    private final String TAG_PHONE = "phone";


    public RecieveElementAdapter(Context context, OnDeleteItemClickListner contactFragment, JSONArray jsonArray, boolean isDataAvailable) {
        this.context = context;
        this.contactFragment = contactFragment;
        if (isDataAvailable) {
            listLength = jsonArray.length();
        } else {
            listLength = 0;
        }

        nickNameList = new ArrayList<>();
        fullNameList = new ArrayList<>();
        numberList = new ArrayList<>();
        emailList = new ArrayList<>();
        for (int i = 0; i < listLength; i++) {
            JSONObject jsonobject;
            String nickName = null;
            String fullName = null;
            String phNumber = null;
            String email = null;
            try {
                jsonobject = (JSONObject) jsonArray.get(i);
                nickName = jsonobject.optString(TAG_NICKNAME);
                fullName = jsonobject.optString(TAG_FIRST_NAME) + " " + //
                        jsonobject.optString(TAG_LAST_NAME);
                phNumber = jsonobject.optString(TAG_PHONE);
                email = jsonobject.optString(TAG_EMAIL);
                nickNameList.add(nickName);
                fullNameList.add(fullName);
                numberList.add(phNumber);
                emailList.add(email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return listLength;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.receiving_element_item, null);

        ImageView ivProfilePic = (ImageView) convertView.findViewById(R.id.elementPic);
        TextView name = ((TextView) convertView.findViewById(R.id.elementName));
        name.setText("" + nickNameList.get(position));
        TextView fullName = ((TextView) convertView.findViewById(R.id.elementFullName));
        fullName.setText("" + fullNameList.get(position));
        TextView number = ((TextView) convertView.findViewById(R.id.elementNumber));
        number.setText("" + numberList.get(position));
        //if (contactFragment.isEditing()) {
        //number.setText("" + numberList.get(position));
        convertView.findViewById(R.id.elementDelete).setVisibility(View.GONE);
        convertView.findViewById(R.id.elementEdit).setVisibility(View.GONE);
        convertView.findViewById(R.id.elementDelete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactFragment instanceof OnDeleteItemClickListner) {
                    // contactFragment.onDeleteItemClick(position);
                }
            }
        });

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DadApp");

        Bitmap bitmap = BitMapHelper.loadImageFromStorage(context, "" + emailList.get(position), directory.toString());


        if (bitmap == null) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.pf_pic);
            ((ImageView) convertView.findViewById(R.id.elementPic)).setBackgroundDrawable(drawable);

        } else {
            ((ImageView) convertView.findViewById(R.id.elementPic)).setBackgroundDrawable(new BitmapDrawable(BitMapHelper.getCircleBitmap(bitmap)));
        }
//
//        if (bitmap == null) {
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pf_pic));
//        } else {
//            Bitmap circledBitmap = createScaleddBitmapFromFile(bitmap);
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setImageDrawable(new BitmapDrawable(circledBitmap));
//        }

        return convertView;
    }

    private Bitmap createScaleddBitmapFromFile(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(scaledBitmap);
        return croppedBitmap;
    }

}
