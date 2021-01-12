package com.sum10.escape;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ManageActivity extends AppCompatActivity {

    private String theme;
    private EditText hintcode_text;
    private EditText hint_text;
    private EditText answer_text;
    private ImageView hint_image;
    private ImageView hint_image2;
    private int hintcode;
    private int num = 0;
    private String hint;
    private String answer;
    private String imageuri;
    private String imageuri2;
    private DBHelper dbHelper; //SQLite 클래스
    private final int GALLERY_CODE = 1112; //갤러리 접근 권한 코드
    private final int REQUEST_PERMISSION_CODE = 2222; //퍼미션 요청 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        theme = intent.getStringExtra("theme"); //이전 액티비티에서 리스트 이름을 가져와 theme에 저장
        setTitle(theme); //타이틀바 테마이름으로 설정
        setContentView(R.layout.activity_manage);

        Button add_button = findViewById(R.id.addhint);
        Button delete_button = findViewById(R.id.deletehint);
        Button list_button = findViewById(R.id.hintlist);
        Button image_button = findViewById(R.id.imagebutton);
        Button image_button2 = findViewById(R.id.imagebutton2);
        hintcode_text = findViewById(R.id.hintcodeedit);
        hint_text = findViewById(R.id.hintedit);
        answer_text = findViewById(R.id.answeredit);
        hint_image = findViewById(R.id.hintimage);
        hint_image2 = findViewById(R.id.hintimage2);

        dbHelper = new DBHelper(getApplicationContext(), "theme.db", null, 1); //theme.db 이름으로 데이터베이스 생성
        hintcode = 0;
        hint = null;
        answer = null;
        imageuri = null;

        //추가 버튼 클릭 시
        add_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hintcode_text.getText().toString().equals("") && !answer_text.getText().toString().equals("")) {
                    hintcode = Integer.parseInt(hintcode_text.getText().toString());
                    //hint = hint_text.getText().toString();
                    hint = "a";
                    answer = answer_text.getText().toString(); //각각 변수에 EditText에 있던 텍스트를 저장
                    dbHelper.insert(theme, hintcode, hint, answer, imageuri, imageuri2); //데이터베이스에 저장
                    hintcode_text.setText("");
                    hint_text.setText("");
                    answer_text.setText(""); // EditText 초기화
                    hint_image.setImageResource(0);
                    hint_image2.setImageResource(0);
                    Toast.makeText(getApplicationContext(), "힌트 정보가 저장되었습니다.", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "힌트코드 또는 정답이 비워져 있습니다.", Toast.LENGTH_LONG).show();
            }
        });

        //삭제 버튼 클릭 시
        delete_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hintcode_text.getText().toString().equals("")) {
                    hintcode = Integer.parseInt(hintcode_text.getText().toString());
                    dbHelper.delete(theme, hintcode); //현재 테마에서 힌트코드와 일치하는 행을 데이터베이스에서 삭제
                    Toast.makeText(getApplicationContext(), "힌트 정보가 삭제되었습니다.", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "힌트코드를 입력하세요.", Toast.LENGTH_LONG).show();
            }
        });

        //힌트 목록 버튼 클릭 시
        list_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_list = new AlertDialog.Builder(ManageActivity.this); //다이얼로그로 힌트목록 표시
                alert_list.setTitle("현재 테마의 힌트코드-정답");
                TextView textView = new TextView(ManageActivity.this);
                textView.setTextSize(30);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setText(dbHelper.getManageList(theme)); //현재 테마에 해당하는 데이터베이스들만 불러옴
                alert_list.setView(textView);

                alert_list.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert_list.show();
            }
        });

        //사진 추가 버튼 클릭 시
        image_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(ManageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                num = 1;
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) //퍼미션 허용할 경우
                    selectGallery();
                else {
                    requestPermission();
                    Toast.makeText(ManageActivity.this, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        image_button2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(ManageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                num = 2;
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) //퍼미션 허용할 경우
                    selectGallery();
                else {
                    requestPermission();
                    Toast.makeText(ManageActivity.this, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //여기서부터 갤러리 접근 메소드들
    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE); //이미지 타입들만 표시하는 갤러리로 이동
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_CODE) {
                if (num == 1) {
                    imageuri = data.getDataString();
                }
                else {
                    imageuri2 = data.getDataString();
                }
                sendPicture(data.getData());
            }
        }
    }

    private void sendPicture(Uri imgUri) {
        String imagePath = getRealPathFromURI(imgUri);
        ExifInterface exifInterface = null;

        try {
            exifInterface = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert exifInterface != null;
        int exifOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (num == 1)
            hint_image.setImageBitmap(rotate(bitmap, exifDegree)); //이미지와 각도를 가져와 표시
        else
            hint_image2.setImageBitmap(rotate(bitmap, exifDegree)); //이미지와 각도를 가져와 표시
    }

    private int exifOrientationToDegrees(int exifOrientation) { //이미지의 절대경로(각도)를 가져옴
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;
        return 0;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        assert cursor != null;
        if (cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //여기서부터 퍼미션 처리 메소드들
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectGallery();
            else {
                Toast.makeText(this, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
