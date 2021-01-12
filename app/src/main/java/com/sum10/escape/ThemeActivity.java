package com.sum10.escape;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;

public class ThemeActivity extends AppCompatActivity {

    private int currentApiVersion;
    private SharedPreferences preferences;
    private String theme;
    private long timer = 3600000;
    private CountDownTimer countDownTimer;
    private String hintcode = null;
    private String hint = null;
    private ImageView theme_image;
    private String uri;
    private String imageuri = "-";
    private String imageuri2 = "-";
    private String answer = "-";
    private int count;
    private int maxcount;
    private boolean language = false;
    private Button start_button;
    private Button hint_button;
    private Button hint1_button;
    private Button hint2_button;
    private Button answer_button;
    private TextView hintcount_text;
    private TextView hint_text;
    private TextView answer_text;
    private EditText code_text;
    private EditText timer_text;
    private ImageView hint_image;
    private ImageView hint_image2;
    private DBHelper dbHelper;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent(); // 인텐트 선언, 초기화
        theme = intent.getStringExtra("theme"); // 테마선택화면에서 테마 이름을 가져와 theme 문자열에 저장
        setTitle(""); // 타이틀을 theme 문자열로 설정
        setContentView(R.layout.activity_theme);
        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        dbHelper = new DBHelper(getApplicationContext(), "theme.db", null, 1);

        hint_button = findViewById(R.id.hintbutton);
        start_button = findViewById(R.id.startbutton);
        answer_button = findViewById(R.id.answerbutton);
        hint1_button = findViewById(R.id.hintbutton1);
        hint2_button = findViewById(R.id.hintbutton2);
        hintcount_text = findViewById(R.id.hintcount);
        hint_text = findViewById(R.id.hinttext);
        answer_text = findViewById(R.id.answertext);
        code_text = findViewById(R.id.hintcode);
        timer_text = findViewById(R.id.timer);
        hint_image = findViewById(R.id.image);
        hint_image2 = findViewById(R.id.image2);
        theme_image = findViewById(R.id.image3);
        TextView title_text = findViewById(R.id.titletext);
        title_text.setText(theme); //타이틀을 theme 문자열 설정
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        maxcount = preferences.getInt("maxcount", 5); // preference에 힌트 최대 갯수(maxcount)를 불러와 저장. 없을 시 기본 5로 가져옴

        hint_image.setImageResource(0);
        hint_image2.setImageResource(0);
        hintcount_text.setText("사용 힌트 수 :  " + count + " / " + maxcount);
        hint1_button.setEnabled(false);
        hint2_button.setEnabled(false);

        uri = (dbHelper.selectImg(theme)); // DB에서 선택된 테마의 이미지 링크를 가져와 uri 변수에 저장
        Log.d("URITAG", uri);
        theme_image.setImageBitmap(null);
        if (!uri.equals("-") && !uri.equals("null"))
            sendPicture(Uri.parse(uri), 0); // 이미지를 이미지뷰에 뿌림

        hint_button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!code_text.getText().toString().equals("")) { // 힌트코드 텍스트뷰가 비워져있지 않을 때
                    hintcode = code_text.getText().toString();
                    hint = dbHelper.getHint(theme, Integer.parseInt(hintcode)); // DB에서 힌트코드와 일치하는 힌트 설명을 가져옴
                    imageuri = dbHelper.getImageUri(theme, Integer.parseInt(hintcode)); // DB에서 힌트코드와 일치하는 이미지 경로를 가져옴
                    imageuri2 = dbHelper.getImageUri2(theme, Integer.parseInt(hintcode));
                    answer = dbHelper.getAnswer(theme, hintcode); // 현재 뿌려진 힌트 설명에 맞는 정답을 가져옴
                    Log.d("Log", imageuri);
                    Log.d("Log", imageuri2);
                    //code_text.setText(""); // 입력한 힌트코드 날림
                    answer_text.setText(""); // 기존에 띄워진 정답 날림
                    hint_image.setImageResource(0);
                    hint_image2.setImageResource(0);
                    if (!hint.equals("-")) { // 힌트가 있을 경우에만
                        //hint_text.setText(hint); // 힌트 설명을 텍스트뷰에 뿌림
                        hint1_button.setEnabled(true);
                        hint2_button.setEnabled(true);
                        count++; //힌트를 사용할 때마다 count가 올라감

                        hintcount_text.setText("사용 힌트 수 :  " + count + " / " + maxcount);
                        Toast.makeText(getApplicationContext(), "힌트 코드 사용! 버튼을 눌러 확인하세요.", Toast.LENGTH_LONG).show();
                        imm.hideSoftInputFromWindow(code_text.getWindowToken(), 0);
                    } else
                        Toast.makeText(getApplicationContext(), "힌트 코드가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "힌트 코드를 입력하세요.", Toast.LENGTH_LONG).show();

                /*if (count == maxcount) { // 사용한 힌트가 maxcount와 일치할 경우(힌트를 다 사용할 경우) 힌트 버튼과 정답 버튼을 비활성화
                    hint_button.setEnabled(false);
                    answer_button.setEnabled(false);
                }*/
            }
        });

        hint1_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imageuri.equals("-") && !imageuri.equals("null"))
                    sendPicture(Uri.parse(imageuri), 1); // 이미지를 이미지뷰에 뿌림
                else
                    hint_image.setImageResource(0);
                code_text.setText(""); // 입력한 힌트코드 날림
                hint1_button.setEnabled(false);
            }
        });

        hint2_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imageuri2.equals("-") && !imageuri2.equals("null"))
                    sendPicture(Uri.parse(imageuri2), 2); // 이미지를 이미지뷰에 뿌림
                else
                    hint_image2.setImageResource(0);
                code_text.setText(""); // 입력한 힌트코드 날림
                hint2_button.setEnabled(false);
            }
        });

        answer_button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) { // 정답버튼 클릭시
                //answer = dbHelper.getAnswer(theme, hintcode); // 현재 뿌려진 힌트 설명에 맞는 정답을 가져옴
                hintcode = null;
                hint_text.setText(""); // 정답이 보여지면 힌트는 필요없기 떄문에 날림
                hint_image.setImageResource(0); // 이미지도 같이 날림
                hint_image2.setImageResource(0); // 이미지도 같이 날림

                if (!answer.equals("-")) {
                    //count++; // 정답을 사용해도 count가 올라감
                    hintcount_text.setText("사용 힌트 수 :  " + count + " / " + maxcount);
                    answer_text.setText(answer); // 정답을 텍스트뷰에 뿌림
                    code_text.setText("");
                    hint1_button.setEnabled(false);
                    hint2_button.setEnabled(false);
                }
                if (count == maxcount) {
                    hint_button.setEnabled(false);
                    hint1_button.setEnabled(false);
                    hint2_button.setEnabled(false);
                    answer_button.setEnabled(false);
                }
            }
        });

        start_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start_button.getText().toString().equals("탈출")) {
                    AlertDialog.Builder alert_finish = new AlertDialog.Builder(ThemeActivity.this);
                    alert_finish.setTitle("탈출하시겠습니까? 탈출 이후에는 어떤 기능도 동작하지 않습니다.");
                    alert_finish.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            start_button.setText("탈출 완료");
                            countDownTimer.cancel();
                            start_button.setEnabled(false);
                            hint_button.setEnabled(false);
                            hint_image.setEnabled(false);
                            answer_button.setEnabled(false);
                        }
                    });
                    alert_finish.show();
                } else {
                    timer = Long.parseLong(timer_text.getText().toString()) * 60000;
                    timer_text.setEnabled(false);
                    countDownTimer();
                    countDownTimer.start(); // 타이머 시작
                    start_button.setText("탈출");
                    /*final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;*/

                    // This work only for android 4.4+
                    /*if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

                        getWindow().getDecorView().setSystemUiVisibility(flags);

                        final View decorView = getWindow().getDecorView();
                        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                    decorView.setSystemUiVisibility(flags);
                                }
                            }
                        });
                    }*/
                }
            }
        });

        hint_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) { // 힌트 이미지가 잘 안보여서 클릭할 경우
                AlertDialog.Builder alert_image = new AlertDialog.Builder(ThemeActivity.this); // 다이얼로그 하나 생성
                //ImageView imageView = new ImageView(ThemeActivity.this); // 다이얼로그에 이미지뷰 생성
                PhotoView photoView = new PhotoView(ThemeActivity.this); // 다이얼로그에 포토뷰 생성(핀치줌/아웃 가능)
                BitmapDrawable alertimage = (BitmapDrawable) hint_image.getDrawable();
                if (alertimage != null) {
                    Bitmap tmpBitmap = alertimage.getBitmap();
                    //imageView.setImageBitmap(tmpBitmap);
                    photoView.setImageBitmap(tmpBitmap);
                    //alert_image.setView(imageView); // 다이얼로그 이미지뷰에 이미지를 띄워서 팝업처럼 보이게 함
                    alert_image.setView(photoView);
                    alert_image.show();
                } else
                    Toast.makeText(getApplicationContext(), "현재 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        hint_image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) { // 힌트 이미지가 잘 안보여서 클릭할 경우
                AlertDialog.Builder alert_image = new AlertDialog.Builder(ThemeActivity.this); // 다이얼로그 하나 생성
                //ImageView imageView = new ImageView(ThemeActivity.this); // 다이얼로그에 이미지뷰 생성
                PhotoView photoView = new PhotoView(ThemeActivity.this); // 다이얼로그에 포토뷰 생성(핀치줌/아웃 가능)
                BitmapDrawable alertimage = (BitmapDrawable) hint_image2.getDrawable();
                if (alertimage != null) {
                    Bitmap tmpBitmap = alertimage.getBitmap();
                    //imageView.setImageBitmap(tmpBitmap);
                    photoView.setImageBitmap(tmpBitmap);
                    //alert_image.setView(imageView); // 다이얼로그 이미지뷰에 이미지를 띄워서 팝업처럼 보이게 함
                    alert_image.setView(photoView);
                    alert_image.show();
                } else
                    Toast.makeText(getApplicationContext(), "현재 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTimer() {
        int minute = (int) timer / 60000;
        int seconds = (int) timer % 60000 / 1000;
        String timeleftText;

        timeleftText = "" + minute;
        timeleftText += ":";
        if (seconds < 10) timeleftText += "0";
        timeleftText += seconds;

        timer_text.setText(timeleftText);
    }

    public void countDownTimer() {
        countDownTimer = new CountDownTimer(timer, 1000) {
            @Override
            public void onTick(long l) {
                timer = l;
                updateTimer();
            }

            public void onFinish() { // 타이머 종료시
                ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                tone.startTone(ToneGenerator.TONE_DTMF_S, 3000);
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //moveTaskToBack(true);
                start_button.setText("시간 종료");
                start_button.setEnabled(false);
                hint_button.setEnabled(false);
                hint_image.setEnabled(false);
                answer_button.setEnabled(false);
                //finish();

                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
    }

    @Override
    public void onBackPressed() { // 타이머가 켜진 경우에는 뒤로가기 누를 수 없게 설정
        start_button = findViewById(R.id.startbutton);
        if (!start_button.getText().toString().equals("탈출")) {
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "탈출 도중에는 불가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // 우측 상단 메뉴 표시
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_theme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 메뉴 선택 시
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        final String password = preferences.getString("pw", "0000");
        switch (item.getItemId()) {
            case R.id.action_themesetting: // 관리자 설정 선택
                final AlertDialog.Builder alert_checkpw = new AlertDialog.Builder(ThemeActivity.this);
                alert_checkpw.setTitle("비밀번호를 입력하세요.");
                final EditText pw_text = new EditText(ThemeActivity.this);
                pw_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alert_checkpw.setView(pw_text);

                alert_checkpw.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (password.equals(pw_text.getText().toString())) {
                            final AlertDialog.Builder alert_choose = new AlertDialog.Builder(ThemeActivity.this);
                            final ArrayAdapter<String> adapter = new ArrayAdapter<>(ThemeActivity.this, android.R.layout.select_dialog_singlechoice);
                            adapter.addAll("힌트 관리", "최대 힌트수 변경");
                            alert_choose.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // 관리자 설정 화면으로 넘어감
                                            Intent intent = new Intent(ThemeActivity.this, ManageActivity.class);
                                            intent.putExtra("theme", theme);
                                            startActivity(intent);
                                            break;
                                        case 1: // 최대 힌트수를 변경할 수 있는 코드
                                            final AlertDialog.Builder alert_changehint = new AlertDialog.Builder(ThemeActivity.this);
                                            alert_changehint.setTitle("최대 힌트 수를 입력해주세요.");
                                            final EditText ch_hint = new EditText(ThemeActivity.this);
                                            alert_changehint.setView(ch_hint);

                                            alert_changehint.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SharedPreferences.Editor editor;
                                                    editor = preferences.edit();
                                                    maxcount = Integer.parseInt(ch_hint.getText().toString());
                                                    editor.remove("maxcount"); // 기존 maxcount를 삭제
                                                    editor.putInt("maxcount", maxcount); // 새로운 maxcount를 저장
                                                    hintcount_text.setText("사용 힌트 수 :  " + count + " / " + maxcount);
                                                    editor.commit();
                                                    Toast.makeText(getApplicationContext(), "힌트 갯수가 변경되었습니다.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            alert_changehint.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alert_changehint.show();
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
            case R.id.action_conversion: // 외국인을 위한 한/영 변환을 선택 시. boolean값인 language가 0일 땐 한국어, 1일 땐 영어 표시
                if (!language) {
                    hintcount_text.setText("hint count  :  " + count + " / " + maxcount);
                    code_text.setHint("Input the code");
                    hint_button.setText("Take hint");
                    answer_button.setText("Check answer");
                    start_button.setText("Start");
                    hint1_button.setText("Hint 1");
                    hint2_button.setText("Hint 2");
                    language = true;
                } else {
                    hintcount_text.setText("사용한 힌트 수 :  " + count + " / " + maxcount);
                    code_text.setHint("힌트 코드를 입력하세요");
                    hint_button.setText("힌트 확인");
                    answer_button.setText("정답 보기");
                    start_button.setText("시작");
                    hint1_button.setText("힌트 1");
                    hint2_button.setText("힌트 2");
                    language = false;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 여기서부터는 갤러리 접근 메소드들
    private void sendPicture(Uri imgUri, int i) {
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
        if (i == 0)
            theme_image.setImageBitmap(rotate(bitmap, exifDegree));
        else if (i == 1)
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
}
