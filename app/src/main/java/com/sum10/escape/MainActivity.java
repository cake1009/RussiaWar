package com.sum10.escape;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private TextView theme_text;
    private ImageView theme_image;
    private Spinner theme_list;
    private DBHelper dbHelper;
    private String theme;
    private String themetime;
    private String uri;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("preferences", MODE_PRIVATE); // 관리자 비밀번호 저장용 preference
        password = preferences.getString("pw", "0000"); // 비밀번호를 preference에 pw 이름으로 저장된 값을 불러옴. 없을 시 0000으로 가져옴
        dbHelper = new DBHelper(getApplicationContext(), "theme.db", null, 1); //내부 데이터베이스에 theme.db라는 파일을 생성

        theme_text = findViewById(R.id.themetext);
        theme_image = findViewById(R.id.themeimage);
        theme_list = findViewById(R.id.themelist);
        Button theme_start = findViewById(R.id.themestart);

        theme_text.setMovementMethod(new ScrollingMovementMethod()); // 스크롤 가능하게 설정

        ArrayList<String> namelist = dbHelper.getTheme(); // 내부 DB에서 저장된 테마 문자열들을 가져와 리스트에 저장

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, namelist); // 테마 문자열 리스트와 스피너를 연결
        theme_list.setAdapter(adapter); // 스피너에 테마 이름들을 뿌려줌
        theme_list.setSelection(0); // 0번 항목을 기본으로 설정
        theme_list.bringToFront();

        theme_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // 스피너 아이템 선택했을 떄
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                theme = theme_list.getItemAtPosition(position).toString(); // 테마 이름을 theme 변수에 저장
                theme_text.setText(dbHelper.selectText(theme)); // DB에서 선택된 테마의 설명을 가져와 텍스트뷰에 뿌림
                themetime = dbHelper.selectTime(theme);
                uri = (dbHelper.selectImg(theme)); // DB에서 선택된 테마의 이미지 링크를 가져와 uri 변수에 저장
                Log.d("URITAG", uri);
                theme_image.setImageBitmap(null);
                if (!uri.equals("null")) // 이미지가 있을 때만 이미지뷰에 뿌리게 설정
                    sendPicture(Uri.parse(uri)); // 이미지 뿌려줌
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        theme_start.setOnClickListener(new Button.OnClickListener() { // 테마 시작을 눌렀을 때
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert_checkpw = new AlertDialog.Builder(MainActivity.this); // 다이얼로그 생성
                alert_checkpw.setTitle("비밀번호를 입력하세요.");
                final EditText pw_text = new EditText(MainActivity.this); // EditText뷰를 하나 생성
                pw_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //텍스트뷰를 비밀번호 입력 타입으로 설정(****)
                alert_checkpw.setView(pw_text);

                alert_checkpw.setPositiveButton("확인", new DialogInterface.OnClickListener() { // 확인 버튼을 눌렀을 떄
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (password.equals(pw_text.getText().toString())) { // 입력한 비밀번호와 현재 preference에 저장된 비밀번호 비교
                            Intent intent = new Intent(MainActivity.this, ThemeActivity.class);// 테마페이지로 이동할 때 가져갈 데이터(이름, 힌트 등)에 필요한 인텐트 선언
                            intent.putExtra("theme", theme); // 테마 이름을 다음 화면으로 가져가기 위한 코드
                            intent.putExtra("themetime", themetime);
                            startActivity(intent); // 다음 화면 시작
                        } else
                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
                alert_checkpw.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // 우측 상단 메뉴 표시
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 우측 상단 메뉴를 선택했을 떄
        switch (item.getItemId()) {
            case R.id.action_mainsetting: // 관리자 설정 선택시
                final AlertDialog.Builder alert_checkpw = new AlertDialog.Builder(MainActivity.this);
                alert_checkpw.setTitle("비밀번호를 입력하세요.");
                final EditText pw_text = new EditText(MainActivity.this);
                pw_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alert_checkpw.setView(pw_text);

                alert_checkpw.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (password.equals(pw_text.getText().toString())) {
                            final AlertDialog.Builder alert_choose = new AlertDialog.Builder(MainActivity.this);
                            final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                            adapter.addAll("테마 관리", "비밀번호 변경"); // 비밀번호 일치할 때 두 가지 선택화면을 띄움
                            alert_choose.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // 테마 관리 선택시
                                            Intent intent = new Intent(MainActivity.this, Manage2Activity.class);
                                            startActivity(intent); // 관리자 설정화면으로 이동
                                            break;
                                        case 1:
                                            final AlertDialog.Builder alert_changepw = new AlertDialog.Builder(MainActivity.this);
                                            alert_changepw.setTitle("변경할 비밀번호를 입력하세요.");
                                            final EditText ch_pw = new EditText(MainActivity.this);
                                            alert_changepw.setView(ch_pw);

                                            alert_changepw.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SharedPreferences.Editor editor; // preference 값을 수정/추가할 수 있는 Editor 선언
                                                    editor = preferences.edit(); // editor를 preference와 연결
                                                    password = ch_pw.getText().toString();
                                                    editor.remove("pw"); // 기존 pw 삭제
                                                    editor.putString("pw", password); // 새로운 pw 저장
                                                    editor.commit(); // preference 저장 코드. 마지막에 항상 필요
                                                    Toast.makeText(getApplicationContext(), "비밀번호를 변경했습니다.", Toast.LENGTH_LONG).show(); // 토스트 메시지 출력
                                                }
                                            });
                                            alert_changepw.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alert_changepw.show();
                                            break;
                                        default:
                                    }
                                }
                            });
                            alert_choose.show();
                        } else
                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
                alert_checkpw.show();
                return true;
            case R.id.action_resetpw:
                final AlertDialog.Builder alert_reset = new AlertDialog.Builder(MainActivity.this);
                alert_reset.setTitle("비밀번호 초기화");
                alert_reset.setMessage("비밀번호를 초기화하시겠습니까? 설정된 초기 비밀번호로 변경됩니다.");
                alert_reset.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor;
                        editor = preferences.edit();
                        editor.remove("pw"); // 기존 pw 삭제
                        password = "0000";
                        editor.commit();
                        Toast.makeText(getApplicationContext(), "비밀번호를 초기화했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                alert_reset.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert_reset.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 여기서부터 갤러리 접근 메소드들
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
        theme_image.setImageBitmap(rotate(bitmap, exifDegree)); //이미지와 각도를 가져와 표시
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
}
