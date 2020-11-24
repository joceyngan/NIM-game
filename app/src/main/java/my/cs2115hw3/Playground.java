package my.cs2115hw3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Playground extends AppCompatActivity implements View.OnClickListener {
    private Canvas canvas;
    private Bitmap bitmap;
    private ImageView imageView;
    private TextView player_tv;

    private final int[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.BLACK};
    private int[] ballNum;
    private int colorNum = 1;
    private int[] currentMove;
    private int moveCount = 0;
    private int lastColor = -1;
    private int currentColor = -2;
    private boolean currentPlayer = true;
    private final List<Button> buttonList = new ArrayList<>();

    private int[][] positions;
    private ArrayList<Integer> PList = new ArrayList<>();
    private ArrayList<Integer> StablePList = new ArrayList<>();
    private ArrayList<int[]> StablePNumber = new ArrayList<>();

    private Handler mUI_Handler = new Handler();
    private Handler mThreadHandler;
    private HandlerThread mThread;

    private MyAdapter myAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);
        Intent intent = getIntent();
        colorNum = intent.getIntExtra("colorNum", 2);
        init();


        mThread = new HandlerThread("Worker");
        mThread.start();
        mThreadHandler=new Handler(mThread.getLooper());
        mThreadHandler.post(getP);
    }

    private final Runnable getP = new Runnable() {
        @Override
        public void run() {
            getPosition();
        }
    };

    private final Runnable updateList = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 10000; i++){
                if (positions[i][5] == 0){
                    if(!StablePList.contains(i)){
                        StablePList.add(i);
                        StablePNumber.add(positions[i]);
                    }
                }
            }
            recyclerView = findViewById(R.id.recycView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            myAdapter = new MyAdapter(StablePList, positions);
            recyclerView.setAdapter(myAdapter);
        }
    };

    private void getPosition() {
        int a = 0, b = 0, c = 0, d = 0, e = 0;
        System.out.println(positions.length);
        for (int i = 0; i < positions.length; i++) {
            positions[i][0] = a;
            positions[i][1] = b;
            positions[i][2] = c;
            positions[i][3] = d;
            positions[i][4] = e;
            positions[i][5] = 0;
            a++;
            if(a > 9){
                a = 0;
                b++;
            }
            if(b > 9){
                b = 0;
                c++;
            }
            if(c > 9){
                c = 0;
                d++;
            }
            if(d > 9){
                d = 0;
                e++;
            }
        }

        for(int i =1; i< positions.length; i++){
            if(i % 5000 == 0){
                mUI_Handler.post(updateList);
                //writeToFile(getApplicationContext(),"tmp_"+i+".txt");  // No write out cheat sheet
            }
            int index;
            for (index = i - 1; index >= 0; index--){
                if (positions[index][5] == 0){
                    if(!PList.contains(index)){
                        PList.add(index);
                    }
                }
            }
            if(multiColor(positions[i], index)){
                positions[i][5] = 1;
            }
            if(singleColor(positions[i], index)){
                positions[i][5] = 1;
            }
        }
        System.out.println("Done");
        writeToFile(getApplicationContext(), "final.txt");

    }

    private void writeToFile(Context context, String name) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE));
            for (int i = 0; i < PList.size(); i++){
                outputStreamWriter.write(positions[PList.get(i)][0] + "," +
                                positions[PList.get(i)][1] + "," +
                                positions[PList.get(i)][2] + "," +
                                positions[PList.get(i)][3] + "," +
                                positions[PList.get(i)][4] + "\n");
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("PLAYGROUND", e.getMessage());
        }
    }


    private boolean singleColor(int[] position, int p) {
        int[] tmpPosition = position.clone();
        boolean find = false;
        for(int i = 0; i < PList.size(); i++){
            if (Arrays.equals(tmpPosition, positions[PList.get(i)])){
                find = true;
            }
        }

        for(int i = 0; i< 5; i++){
            for(int j = 1; j <= tmpPosition[i]; j++){
                if(tmpPosition[i] != 0){
                    tmpPosition[i] = tmpPosition[i] - j;
                }
                for(int k = 0; k < PList.size(); k++){
                    if (Arrays.equals(tmpPosition, positions[PList.get(k)])){
                        find = true;
                    }
                }
                tmpPosition = position.clone();
            }
            if (find) break;
        }
        return find;
    }

    private boolean multiColor(int[] position, int p) {
        int[] tmpPosition = position.clone();
        boolean find = false;
        for(int i = 0; i< 5; i++){
            if(tmpPosition[i] != 0){
                tmpPosition[i] = tmpPosition[i] - 1;
            }
            for(int j = 0; j < 5; j++){
                if (i != j && tmpPosition[j] != 0){
                    tmpPosition[j] = tmpPosition[j] - 1;
                }
                for(int k = 0; k < PList.size(); k++){
                    if (Arrays.equals(tmpPosition, positions[PList.get(k)])){
                        find = true;
                    }
                }
            }
            tmpPosition = position.clone();
            if (find) break;
        }
        return find;
    }


    private void init() {
        imageView = findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        positions = new int[100000][6];
        player_tv = findViewById(R.id.player_tv);



        buttonList.add(findViewById(R.id.blue_btn));
        buttonList.add(findViewById(R.id.red_btn));
        buttonList.add(findViewById(R.id.yellow_btn));
        buttonList.add(findViewById(R.id.green_btn));
        buttonList.add(findViewById(R.id.black_btn));
        findViewById(R.id.next_btn).setOnClickListener(this);
        ballNum = new int[5];
        currentMove = new int[colorNum];
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            buttonList.get(i).setOnClickListener(this);
            if (i < colorNum) {
                ballNum[i] = random.nextInt(6) + 3;
            } else {
                buttonList.get(i).setVisibility(View.INVISIBLE);
            }
        }

        drawGame();
    }

    private void drawGame() {
        Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        int x, y;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
        paint.setColor(Color.WHITE);
        canvas.drawRect(rect, paint);
        for (int i = 0; i < colorNum; i++) {
            y = 30 + i * 50;
            for (int j = 0; j < ballNum[i]; j++) {
                x = 30 + j * 50;
                paint.setColor(colors[i]);
                canvas.drawCircle(x, y, 20, paint);
            }
        }
        imageView.setImageBitmap(bitmap);

        if (currentPlayer) {
                player_tv.setText(getResources().getString(R.string.current_player, 1));
        } else {
                player_tv.setText(getResources().getString(R.string.current_player, 2));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.blue_btn) {
            currentColor = 0;
        }
        if (v.getId() == R.id.red_btn) {
            currentColor = 1;
        }
        if (v.getId() == R.id.yellow_btn) {
            currentColor = 2;
        }
        if (v.getId() == R.id.green_btn) {
            currentColor = 3;
        }
        if (v.getId() == R.id.black_btn) {
            currentColor = 4;
        }
        if (currentColor >= 0) {
            if (ballNum[currentColor] > 0) {
                moveCount++;
                currentMove[currentColor]++;
                ballNum[currentColor]--;

                if (moveCount > 1) {
                    if (lastColor == currentColor) {
                        for (int i = 0; i < colorNum; i++) {
                            if (currentMove[i] == 0) {
                                buttonList.get(i).setVisibility(View.INVISIBLE);
                            }
                        }
                    } else {
                        for (int i = 0; i < colorNum; i++) {
                            if (currentMove[i] != 0) {
                                buttonList.get(i).setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
                lastColor = currentColor;

                drawGame();
            }
            for (int i = 0; i < colorNum; i++) {
                if (ballNum[currentColor] <= 0) {
                    buttonList.get(i).setVisibility(View.INVISIBLE);
                }
            }
            currentColor = -2;
            checkWin();
        }
        if (v.getId() == R.id.next_btn) {
            if (moveCount > 0) {
                for (int i = 0; i < colorNum; i++) {
                    if (ballNum[i] != 0) {
                        buttonList.get(i).setVisibility(View.VISIBLE);
                    }
                    currentMove[i] = 0;
                }
                currentPlayer = !currentPlayer;
                moveCount = 0;
                lastColor = -1;
                currentColor = -2;
                drawGame();
            }
        }


    }

    private void checkWin() {
        boolean win = true;
        for (int i = 0; i < colorNum; i++) {
            if (ballNum[i] > 0) {
                win = false;
                break;
            }
        }
        if (win) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Playground.this);
            if (currentPlayer) {
                alertDialog.setTitle("Player 1 Win");
            } else {
                alertDialog.setTitle("Player 2 Win");
            }
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private ArrayList<Integer> pposition;
    private int[][] positions;

    MyAdapter(ArrayList<Integer> pposition, int[][] positions) {
        this.pposition = pposition;
        this.positions = positions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(positions[pposition.get(position)][0]+ " ," +
                                positions[pposition.get(position)][1]+ " ," +
                                positions[pposition.get(position)][2]+ " ," +
                                positions[pposition.get(position)][3]+ " ," +
                                positions[pposition.get(position)][4]);
    }

    @Override
    public int getItemCount() {
        return pposition.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemView);
        }
    }
}