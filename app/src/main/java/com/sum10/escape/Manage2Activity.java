package com.sum10.escape;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class Manage2Activity extends AppCompatActivity {

    private DBHelper dbHelper; //SQLite 클래스
    private final int GALLERY_CODE = 1112; //갤러리 접근 권한 코드
    private final int REQUEST_PERMISSION_CODE = 2222; //퍼미션 요청 코드
    private String themename;
    private String explain;
    private String themetime;
    private String uri = null;
    private EditText themename_text;
    private EditText time_text;
    private EditText explain_text;
    private ImageView theme_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage2);

        themename_text = findViewById(R.id.nametext);
        explain_text = findViewById(R.id.explaintext);
        theme_img = findViewById(R.id.themeimg);
        time_text = findViewById(R.id.timetext);
        Button add_img = findViewById(R.id.insertimg);
        Button add_theme = findViewById(R.id.addtheme);
        Button delete_theme = findViewById(R.id.deletetheme);

        dbHelper = new DBHelper(getApplicationContext(), "theme.db", null, 1); //theme.db 이름으로 데이터베이스 생성

        //추가 버튼 클릭 시
        add_theme.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                themename = themename_text.getText().toString();
                explain = explain_text.getText().toString();
                themetime = time_text.getText().toString();
                dbHelper.insertTheme(themename, uri, explain, themetime); //데이터베이스에 저장
                Toast.makeText(getApplicationContext(), "테마가 저장되었습니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        //삭제 버튼 클릭 시
        delete_theme.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                themename = themename_text.getText().toString();
                dbHelper.deleteTheme(themename); //현재 테마에서 힌트코드와 일치하는 행을 데이터베이스에서 삭제
                Toast.makeText(getApplicationContext(), "테마가 삭제되었습니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        //사진 추가 버튼 클릭 시
        add_img.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(Manage2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) //퍼미션 허용할 경우
                    selectGallery();
                else {
                    requestPermission();
                    Toast.makeText(Manage2Activity.this, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
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
                uri = data.getDataString();
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
        theme_img.setImageBitmap(rotate(bitmap, exifDegree)); //이미지와 각도를 가져와 표시
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
