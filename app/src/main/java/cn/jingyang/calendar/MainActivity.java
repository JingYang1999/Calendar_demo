package cn.jingyang.calendar;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

public class MainActivity extends AppCompatActivity {
    private int                     v_Y;
    private int                     v_M;
    private int                     events_num;
    private int                     sche_view_beg_flag;
    private int[]                   month;
    private int[]                   today;
    private Button                  submit_btn;
    private TextView                top_Y_M;
    private TextView                WorkingTV;
    private TextView[]              days;
    private TextView[]              events_lines;
    private TextView[]              X_calendar_ctrl;
    private GridLayout              X_calendar;
    private GridLayout              X_schedule;
    private date_event[]            events_list;
    private TextInputLayout         submit_page;
    private TextInputEditText       submit_txt;

    private BottomNavigationView                navi_View_Bottom;
    private View.OnClickListener                ScheduleBtListen;
    private View.OnClickListener                calendar_ctrl_ls;
    private View.OnClickListener                submitbtListener;
    private View.OnLongClickListener            ScheLongCiListen;
    private View.OnLongClickListener            Go_TodayListener;
    private View.OnLongClickListener            addEventListener;
    private OnNavigationItemSelectedListener    NaviSlctListener;

    private   void      Remove_Event    (int evn_id){
        date_event[] t=new date_event[events_num-1];
        for(int i=0;i<evn_id;i++)
            t[i]=events_list[i];
        for(int i=evn_id+1;i<events_num;i++)
            t[i-1]=events_list[i];
        events_num-=1;
        events_list=t;
    }
    private   boolean   DateEventCMP    (date_event a,date_event b){
        //若 a 晚于 b 返回 true, 日期相同则比较 info 文本的长度若 a 长于 b 返回 true
        if(a.date[0]>b.date[0])                 return true;
        else if(a.date[0]<b.date[0])            return false;
        else if(a.date[1]>b.date[1])            return true;
        else if(a.date[1]<b.date[1])            return false;
        else if (a.date[2] > b.date[2])         return true;
        else if (a.date[2] < b.date[2])         return false;
        else if(a.info.length()>b.info.length())return true;
        else return false;

    }
    private   void      DateEventSort   (){
        int maxLEN=events_list.length,i,j;
        date_event swap_t;
        for(i=0;i<maxLEN;i++) for(j=1;j<maxLEN;j++)
            if(DateEventCMP(events_list[j-1],events_list[j])){
                    swap_t=events_list[j-1];
                    events_list[j-1]=events_list[j];
                    events_list[j]=swap_t;
                }
    }
    private   void      addEvent        (int yy,int mm,int dd,String ii){
        date_event[] t=new date_event[events_num+1];
        for(int i=0;i<events_num;i++) t[i]=events_list[i];
        t[events_num]=new date_event();
        t[events_num].init(yy,mm,dd,ii);
        events_num+=1;
        events_list=t;
    }
    private   int       QuireEvent      (int yy,int mm,int dd){
        for(int i=0;i<events_num;i++)
            if(events_list[i].date[0]==yy&&events_list[i].date[1]==mm&&events_list[i].date[2]==dd)
                return i;
        return -1;
    }
    private   boolean   hasEvent        (int yy,int mm,int dd){
        for(int i=0;i<events_num;i++)
            if(events_list[i].date[0]==yy&&events_list[i].date[1]==mm&&events_list[i].date[2]==dd)
                return true;
        return false;
    }
    private   boolean   is_run          (int yy) {
        if(yy%400==0 ||(yy%100!=0&&yy%4==0))return true;
        return false;
    }
    protected void      onCreate        (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        events_lines    =   new TextView[8];
        X_calendar_ctrl =   new TextView[4];

        navi_View_Bottom=   findViewById(R.id.nav_view);
        X_calendar      =   findViewById(R.id.calendar);
        X_schedule      =   findViewById(R.id.schedules);
        submit_page     =   findViewById(R.id.submit_page);
        init_Listeners();
        init_X_calendar();
        init_X_schedule();
        if(navi_View_Bottom!=null)  navi_View_Bottom.setOnNavigationItemSelectedListener(NaviSlctListener);
        load_X_calendar();

        //addTestDate();
    }
    private   int       get_first_day   (int yy,int mm){ //已知1970年1月1日是周四
        int i=0,sum_of_day=0;
        for(i=1970;i<yy;i++) if (is_run(i)) sum_of_day += 366; else sum_of_day += 365;
        for(i=1;i<mm;i++) sum_of_day+=get_number_day(yy,i);
        return (sum_of_day+4)%7;
    }
    private   int       get_number_day  (int yy,int mm) {
        if(mm==1 || mm==3 ||mm==5|| mm==7|| mm==8|| mm==10|| mm==12) return 31;
        if(mm==4|| mm==6|| mm==9|| mm==11)return 30;
        if(is_run(yy)) return 29;
        return 28;
    }
    private   void      init_Listeners  (){
        ScheLongCiListen=new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView tv=(TextView)v;
                if (tv.getText().equals(getText(R.string.up))||tv.getText().equals(getText(R.string.down))) return false;
                int id=-1;
                for(int i=0;i<8;i++)
                    if(v.getId()==events_lines[i].getId())
                        id=i;
                if(id!=-1){
                    Remove_Event((id+sche_view_beg_flag));
                    sche_view_beg_flag=0;
                    load_X_schedule();
                    return true;
                }
                return false;
            }
        };
        ScheduleBtListen=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv=(TextView)v;
                if(tv.getText().equals(getText(R.string.up)))
                    //if(sche_view_beg_flag>0)
                        sche_view_beg_flag-=1;
                if(tv.getText().equals(getText(R.string.down)))
                    //if(sche_view_beg_flag<events_num-8)
                        sche_view_beg_flag+=1;
                load_X_schedule();
                return;
            }
        };
        submitbtListener=new View.OnClickListener(){
            public void onClick(View v) {
                if(submit_txt.getText().toString().equals(""))return;
                else{
                    int dd=Integer.parseInt((String)WorkingTV.getText());
                    WorkingTV=null;
                    addEvent(v_Y,v_M,dd,submit_txt.getText().toString());
                    DateEventSort();

                    load_X_calendar();
                }
            }
        };
        Go_TodayListener= new View.OnLongClickListener(){
            public boolean onLongClick(View v) {
                Calendar cl=Calendar.getInstance();
                today[0]=cl.get(Calendar.YEAR );
                today[1]=cl.get(Calendar.MONTH )+1;
                today[2]=cl.get(Calendar.DAY_OF_MONTH);
                v_Y=today[0];
                v_M=today[1];

                load_X_calendar();
                return false;
            }
        };
        NaviSlctListener = new OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        load_X_calendar();
                        return true;
                    case R.id.navigation_dashboard:
                        load_X_schedule();
                        return true;
                }
                return false;
            }
        };
        calendar_ctrl_ls = new View.OnClickListener() {
            public void onClick(View v) { switch (v.getId()){
                    case R.id.reduce_Y:{
                        v_Y-=1;
                        if(v_Y<1970) v_Y=1970;
                        load_X_calendar();
                        return;
                    }
                    case R.id.reduce_M: {
                        v_M -= 1;
                        if (v_M < 1) { v_Y -= 1; v_M += 12;}
                        if (v_Y < 1970) {v_Y = 1970;v_M = 1;}
                        load_X_calendar();
                        return;
                    }
                    case R.id.increase_M: {
                        v_M += 1;
                        if (v_M > 12) { v_Y += 1; v_M -= 12;}
                        load_X_calendar();
                        return;
                    }
                    case R.id.increase_Y: {
                        v_Y += 1;
                        load_X_calendar();
                        return;
                    }
                }
            }
        };
        addEventListener = new View.OnLongClickListener(){
            public boolean onLongClick(View v) {
                WorkingTV=(TextView) v;
                String s=(String)WorkingTV.getText();
                if(s.equals(""))return false;
                submit_txt.setText("");
                submit_txt.setHint(String.valueOf(v_Y)+getText(R.string.year)+(String)getText(month[v_M-1])+s+getText(R.string.ri)+getText(R.string.dowhat));

                disp(submit_page);
                return false;
            }
        };
    }
    private   void      init_X_calendar (){
        month           =   new int[12];
        today           =   new int[3];
        days            =   new TextView[42];
        X_schedule.setVisibility(View.INVISIBLE);
        X_calendar.setVisibility(View.VISIBLE);

        Calendar cl=Calendar.getInstance();
        today[0]=cl.get(Calendar.YEAR );
        today[1]=cl.get(Calendar.MONTH )+1;
        today[2]=cl.get(Calendar.DAY_OF_MONTH);

        top_Y_M = findViewById(R.id.Y_and_M);
        if(top_Y_M != null) top_Y_M.setOnLongClickListener(Go_TodayListener);

        v_Y=today[0];
        v_M=today[1];

        X_calendar_ctrl[0]=findViewById(R.id.reduce_Y);
        X_calendar_ctrl[1]=findViewById(R.id.reduce_M);
        X_calendar_ctrl[2]=findViewById(R.id.increase_M);
        X_calendar_ctrl[3]=findViewById(R.id.increase_Y);

        month[0]=R.string.jan;
        month[1]=R.string.feb;
        month[2]=R.string.mar;
        month[3]=R.string.apr;
        month[4]=R.string.may;
        month[5]=R.string.june;
        month[6]=R.string.july;
        month[7]=R.string.aug;
        month[8]=R.string.sept;
        month[9]=R.string.oct;
        month[10]=R.string.nov;
        month[11]=R.string.dec;

        days[0]=findViewById(R.id.d00);
        days[1]=findViewById(R.id.d01);
        days[2]=findViewById(R.id.d02);
        days[3]=findViewById(R.id.d03);
        days[4]=findViewById(R.id.d04);
        days[5]=findViewById(R.id.d05);
        days[6]=findViewById(R.id.d06);
        days[7]=findViewById(R.id.d10);
        days[8]=findViewById(R.id.d11);
        days[9]=findViewById(R.id.d12);
        days[10]=findViewById(R.id.d13);
        days[11]=findViewById(R.id.d14);
        days[12]=findViewById(R.id.d15);
        days[13]=findViewById(R.id.d16);
        days[14]=findViewById(R.id.d20);
        days[15]=findViewById(R.id.d21);
        days[16]=findViewById(R.id.d22);
        days[17]=findViewById(R.id.d23);
        days[18]=findViewById(R.id.d24);
        days[19]=findViewById(R.id.d25);
        days[20]=findViewById(R.id.d26);
        days[21]=findViewById(R.id.d30);
        days[22]=findViewById(R.id.d31);
        days[23]=findViewById(R.id.d32);
        days[24]=findViewById(R.id.d33);
        days[25]=findViewById(R.id.d34);
        days[26]=findViewById(R.id.d35);
        days[27]=findViewById(R.id.d36);
        days[28]=findViewById(R.id.d40);
        days[29]=findViewById(R.id.d41);
        days[30]=findViewById(R.id.d42);
        days[31]=findViewById(R.id.d43);
        days[32]=findViewById(R.id.d44);
        days[33]=findViewById(R.id.d45);
        days[34]=findViewById(R.id.d46);
        days[35]=findViewById(R.id.d50);
        days[36]=findViewById(R.id.d51);
        days[37]=findViewById(R.id.d52);
        days[38]=findViewById(R.id.d53);
        days[39]=findViewById(R.id.d54);
        days[40]=findViewById(R.id.d55);
        days[41]=findViewById(R.id.d56);

        for(int i=0;i<4;i++) X_calendar_ctrl[i].setOnClickListener(calendar_ctrl_ls);
        for(int i=0;i<42;i++)days[i].setOnLongClickListener(addEventListener);
        submit_btn=findViewById(R.id.submit_btn);
        submit_txt=findViewById(R.id.submit_txt);
        submit_btn.setOnClickListener(submitbtListener);

        load_X_calendar();
    }
    private   void      init_X_schedule (){
        events_num=0;
        sche_view_beg_flag=0;
        events_lines[0]=findViewById(R.id.s0);
        events_lines[1]=findViewById(R.id.s1);
        events_lines[2]=findViewById(R.id.s2);
        events_lines[3]=findViewById(R.id.s3);
        events_lines[4]=findViewById(R.id.s4);
        events_lines[5]=findViewById(R.id.s5);
        events_lines[6]=findViewById(R.id.s6);
        events_lines[7]=findViewById(R.id.s7);

        for(int i=0;i<8;i++)events_lines[i].setOnLongClickListener(ScheLongCiListen);
        events_lines[0].setOnClickListener(ScheduleBtListen);
        events_lines[7].setOnClickListener(ScheduleBtListen);

        load_X_schedule();
    }
    private   void      load_X_calendar (){
        Calendar cl=Calendar.getInstance();
        today[0]=cl.get(Calendar.YEAR );
        today[1]=cl.get(Calendar.MONTH )+1;
        today[2]=cl.get(Calendar.DAY_OF_MONTH);
        top_Y_M.setText(String.valueOf(v_Y)+(String)getText(R.string.year)+(String)getText(month[v_M-1]));
        this.setTitle(String.valueOf(v_Y)+(String)getText(R.string.year)+(String)getText(month[v_M-1]));
        int i,t=0,offset=get_first_day(v_Y,v_M)%7;
        for(i=0;i<42;i++) {
            days[i].setVisibility(View.VISIBLE);
            days[i].setText("");
            days[i].setTextColor(ContextCompat.getColor(this,R.color.black));
            days[i].setBackgroundColor(ContextCompat.getColor(this,R.color.calendar_background));
        }
        for(i=0;i<get_number_day(v_Y,v_M);i++) {
            t=i+offset;
            days[t].setText(String.valueOf(i+1));
            if(t%7==0||t%7==6)
                days[t].setBackgroundColor(ContextCompat.getColor(this,R.color.white));
            else
                days[t].setBackgroundColor(ContextCompat.getColor(this,R.color.white));
            if(hasEvent(v_Y,v_M,(i+1)))
            {
                days[t].setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
                days[t].setTextColor(ContextCompat.getColor(this,R.color.white));
            }
            if(today[0]==v_Y&&today[1]==v_M&&today[2]==(i+1))
            {
                days[t].setBackgroundColor(ContextCompat.getColor(this,R.color.high_light));
                days[t].setTextColor(ContextCompat.getColor(this,R.color.white));
            }
        }
        disp(X_calendar);
    }
    private   void      load_X_schedule (){
        int i,t;
        this.setTitle(R.string.title_dashboard);
        //this.setTitle(String.valueOf(sche_view_beg_flag));
        if(events_num==0){
            for(i=0;i<8;i++){
                events_lines[i].setText("");
                events_lines[i].setBackgroundColor(ContextCompat.getColor(this,R.color.calendar_background));
            }
            events_lines[3].setText(getText(R.string.empty));
        }
        else if(events_num<9){
            for(i=0;i<events_num;i++){

                events_lines[i].setText(String.valueOf(events_list[i].date[0])+getText(R.string.year)+(String)getText(month[events_list[i].date[1]-1])+String.valueOf(events_list[i].date[2])+getText(R.string.ri)+events_list[i].info);
                events_lines[i].setBackgroundColor(ContextCompat.getColor(this,R.color.white));
            }
            for(i=events_num;i<8;i++){
                events_lines[i].setText("");
                events_lines[i].setBackgroundColor(ContextCompat.getColor(this,R.color.calendar_background));
            }
        }
        else{
            for(i=0;i<8;i++)    events_lines[i].setBackgroundColor(ContextCompat.getColor(this,R.color.white));
            if(sche_view_beg_flag==0) {
                events_lines[0].setText(String.valueOf(events_list[0].date[0])+getText(R.string.year)+(String)getText(month[events_list[0].date[1]-1])+String.valueOf(events_list[0].date[2])+getText(R.string.ri)+events_list[0].info);
            }
            else{
                events_lines[0].setText(getText(R.string.up));
            }
            if(sche_view_beg_flag==events_num-8) {
                t=7+sche_view_beg_flag;
                events_lines[7].setText(String.valueOf(events_list[t].date[0])+getText(R.string.year)+(String)getText(month[events_list[t].date[1]-1])+String.valueOf(events_list[t].date[2])+getText(R.string.ri)+events_list[t].info);
            }
            else{
                events_lines[7].setText(getText(R.string.down));
            }
            for(i=1;i<7;i++) {
                t=i+sche_view_beg_flag;
                events_lines[i].setText(String.valueOf(events_list[t].date[0])+getText(R.string.year)+(String)getText(month[events_list[t].date[1]-1])+String.valueOf(events_list[t].date[2])+getText(R.string.ri)+events_list[t].info);
            }
        }
        disp(X_schedule);
    }
    private   void      disp            (View v){
        switch (v.getId()){
            case R.id.calendar:
                submit_page.setVisibility(View.INVISIBLE);
                X_schedule.setVisibility(View.INVISIBLE);
                X_calendar.setVisibility(View.VISIBLE);
                return;
            case R.id.schedules:
                X_calendar.setVisibility(View.INVISIBLE);
                submit_page.setVisibility(View.INVISIBLE);
                X_schedule.setVisibility(View.VISIBLE);
                return;
            case R.id.submit_page:
                X_calendar.setVisibility(View.INVISIBLE);
                X_schedule.setVisibility(View.INVISIBLE);
                submit_page.setVisibility(View.VISIBLE);
                return;
        }
    }



    private   void      addTestDate     ()
    {
        addEvent(2019,4,1,"吃饭");
        addEvent(2019,5,2,"睡觉");
        addEvent(2019,6,3,"喝水");
        addEvent(2019,7,4,"唱歌");
        addEvent(2019,8,5,"跳舞");
        addEvent(2019,9,6,"逛街");
        addEvent(2019,1,7,"跳绳");
        addEvent(2019,2,8,"打球");
        addEvent(2019,3,9,"跳跃");
        addEvent(2019,4,1,"走路");
        addEvent(2019,5,2,"捉虫");
        addEvent(2019,6,3,"泡面");
    }
}