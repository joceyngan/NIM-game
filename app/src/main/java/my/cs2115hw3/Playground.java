package my.cs2115hw3;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Playground extends AppCompatActivity implements View.OnClickListener {
    private Canvas canvas;
    private Bitmap bitmap;
    private ImageView imageView;
    private TextView player_tv, position_tv;

    private final int[] colors = {Color.BLUE, Color.RED, 0xFFCCCC00, Color.GREEN, Color.BLACK};
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

    private final Handler mUI_Handler = new Handler();

    private boolean isComputer;
    private boolean isDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);
        Intent intent = getIntent();
        colorNum = intent.getIntExtra("colorNum", 2);
        isDemo = intent.getBooleanExtra("isDemo", false);
        isComputer = intent.getBooleanExtra("isComputer", false);
        init();


        HandlerThread mThread = new HandlerThread("Worker");
        mThread.start();
        Handler mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(getP);
    }

    private void init() {
        imageView = findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        positions = new int[100000][6];
        player_tv = findViewById(R.id.player_tv);
        position_tv = findViewById(R.id.positionTV);

        buttonList.add(findViewById(R.id.blue_btn));
        buttonList.add(findViewById(R.id.red_btn));
        buttonList.add(findViewById(R.id.yellow_btn));
        buttonList.add(findViewById(R.id.green_btn));
        buttonList.add(findViewById(R.id.black_btn));
        findViewById(R.id.next_btn).setOnClickListener(this);
        ballNum = new int[5];
        currentMove = new int[colorNum];
        if(!isDemo){
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                buttonList.get(i).setOnClickListener(this);
                if (i < colorNum) {
                    ballNum[i] = random.nextInt(6) + 3;
                } else {
                    buttonList.get(i).setVisibility(View.INVISIBLE);
                }
            }
        }else{ //fixed number for Demo
            for (int i = 0; i < 5; i++) {
                buttonList.get(i).setOnClickListener(this);
                colorNum = 3;
                ballNum[0] = 5;
                ballNum[1] = 3;
                ballNum[2] = 4;
                if (i >= colorNum) {
                    buttonList.get(i).setVisibility(View.INVISIBLE);
                }
            }
        }
        drawGame();
    }

    private final Runnable getP = new Runnable() {
        @Override
        public void run() {
            //getPosition(); Change to read from asset;
            try {
                getPositionFromAsset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final Runnable updateList = new Runnable() {
        @Override
        public void run() {
            /*
            for (int i = 0; i < 10000; i++){
                if (positions[i][5] == 0){
                    if(!StablePList.contains(i)){
                        StablePList.add(i);
                        StablePNumber.add(positions[i]);
                    }
                }
            }
            RecyclerView recyclerView = findViewById(R.id.recycView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            MyAdapter myAdapter = new MyAdapter(StablePList, positions);
            recyclerView.setAdapter(myAdapter);
            */

            RecyclerView recyclerView = findViewById(R.id.recycView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            MyAdapter myAdapter = new MyAdapter(StablePNumber);
            recyclerView.setAdapter(myAdapter);
            int[] find = findNextPosition();
            if(find == null){
                position_tv.setText("Here is P position");
            }else{
                position_tv.setText("Here is N position\nNext P Position is: [" + find[0] + "] [" + find[1] + "] [" + find[2] + "] [" + find[3] + "] [" + find[4] + "]");
                //position_tv.setText("Here is N position");
            }
            Log.e("Log", find[0] + "");

        }
    };

    private void getPositionFromAsset() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("final.txt")));
        String line;
        while ((line = reader.readLine()) != null){
            String[] tmp = line.split(",");
            int[] tmpNum = {Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4])};
            StablePNumber.add(tmpNum);
        }
        mUI_Handler.post(updateList);
    }

    private boolean checkCurrentPosition(){
        for(int i = 0; i<StablePNumber.size(); i++){
            if (Arrays.equals(ballNum, StablePNumber.get(i))){
                return true;
            }
            //Log.e("StablePNumber", ""+StablePNumber.get(i)[0] + StablePNumber.get(i)[1] +StablePNumber.get(i)[2] +StablePNumber.get(i)[3] +StablePNumber.get(i)[4] );
            //Log.e("ballNum", ""+ballNum[0] + ballNum[1] +ballNum[2] +ballNum[3] +ballNum[4] );
        }
        return false;
    }

    private int[] findNextPosition(){
        if (checkCurrentPosition()){
            return null;
        }
        int[] tmpNumber = ballNum.clone();
        Log.e("ballNum", ""+ballNum[0] + ballNum[1] +ballNum[2] +ballNum[3] +ballNum[4] );
        //Multi Rules first
        for (int i = 0; i < 5; i++){
            if(tmpNumber[i] != 0){
                tmpNumber[i] = tmpNumber[i] - 1;
            }
            for(int j = 0; j < 5; j++){
                if (i != j && tmpNumber[j] != 0){
                    tmpNumber[j] = tmpNumber[j] - 1;
                }
                for(int k = 0; k < StablePNumber.size(); k++){
                    if (Arrays.equals(tmpNumber, StablePNumber.get(k))){
                        return tmpNumber;
                    }
                }
            }
            tmpNumber = ballNum.clone();
        }
        //Single Rules
        for(int i = 0; i< 5; i++){
            for(int j = 1; j <= tmpNumber[i]; j++){
                if(tmpNumber[i] != 0){
                    tmpNumber[i] = tmpNumber[i] - j;
                }
                for(int k = 0; k < StablePNumber.size(); k++){
                    if (Arrays.equals(tmpNumber, StablePNumber.get(k))){
                        return tmpNumber;
                    }
                }
                tmpNumber = ballNum.clone();
            }
        }
        return null;
    }

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
                Log.e("ballNum", ""+ballNum[0] + ballNum[1] +ballNum[2] +ballNum[3] +ballNum[4] );
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
                if (ballNum[i] <= 0) {
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
                int[] find = findNextPosition();
                if(find == null){
                    position_tv.setText("Here is P position");
                }else{
                    position_tv.setText("Here is N position\nNext P Position is: [" + find[0] + "] [" + find[1] + "] [" + find[2] + "] [" + find[3] + "] [" + find[4] + "]");
                    //position_tv.setText("Here is N position");
                }
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

}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private ArrayList<int[]> stablePNumber;


    public MyAdapter(ArrayList<int[]> stablePNumber) {
        this.stablePNumber = stablePNumber;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*
        holder.textView.setText(positions[pposition.get(position)][0]+ " ," +
                                positions[pposition.get(position)][1]+ " ," +
                                positions[pposition.get(position)][2]+ " ," +
                                positions[pposition.get(position)][3]+ " ," +
                                positions[pposition.get(position)][4]);
        */
        String tmpString = "[" + stablePNumber.get(position)[0] +
                "] , [" + stablePNumber.get(position)[1]+
                "] , [" + stablePNumber.get(position)[2]+
                "] , [" + stablePNumber.get(position)[3]+
                "] , [" + stablePNumber.get(position)[4]+"]";

        holder.textView.setText(tmpString);
    }

    @Override
    public int getItemCount() {
        return stablePNumber.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemView);
        }
    }
}